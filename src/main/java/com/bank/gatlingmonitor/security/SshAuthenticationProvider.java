package com.bank.gatlingmonitor.security;

import com.bank.gatlingmonitor.config.MonitorProperties;
import com.bank.gatlingmonitor.model.SshCredentials;
import com.bank.gatlingmonitor.service.LoginAttemptService;
import com.bank.gatlingmonitor.service.SshConnectionService;
import com.bank.gatlingmonitor.web.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Component
public class SshAuthenticationProvider implements AuthenticationProvider {

  private final MonitorProperties monitorProperties;
  private final SshConnectionService sshConnectionService;
  private final LoginAttemptService loginAttemptService;

  public SshAuthenticationProvider(
      MonitorProperties monitorProperties,
      SshConnectionService sshConnectionService,
      LoginAttemptService loginAttemptService) {
    this.monitorProperties = monitorProperties;
    this.sshConnectionService = sshConnectionService;
    this.loginAttemptService = loginAttemptService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String clientKey = resolveClientKey();
    if (loginAttemptService.isBlocked(clientKey)) {
      throw new LockedException("Превышено число попыток. Повторите через 1 минуту.");
    }

    String username = authentication.getName();
    String password = authentication.getCredentials().toString();
    SshCredentials credentials = new SshCredentials(username, password);

    List<String> hosts =
        monitorProperties.getGenerators().stream()
            .map(MonitorProperties.Generator::getHost)
            .toList();

    if (!sshConnectionService.canAuthenticateAny(hosts, credentials)) {
      loginAttemptService.loginFailed(clientKey);
      int remaining = loginAttemptService.getRemainingAttempts(clientKey);
      if (remaining == 0) {
        throw new LockedException("locked");
      }
      throw new BadCredentialsException("badCreds");
    }

    loginAttemptService.loginSucceeded(clientKey);
    return new UsernamePasswordAuthenticationToken(
        username,
        password,
        List.of(new SimpleGrantedAuthority("ROLE_MONITOR")));
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private String resolveClientKey() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return "unknown";
    }
    HttpServletRequest request = attributes.getRequest();
    return ClientIpResolver.resolve(request);
  }
}
