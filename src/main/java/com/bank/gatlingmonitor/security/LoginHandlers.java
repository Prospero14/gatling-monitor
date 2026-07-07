package com.bank.gatlingmonitor.security;

import com.bank.gatlingmonitor.service.LoginAttemptService;
import com.bank.gatlingmonitor.web.SessionAttributes;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginHandlers {

  private final LoginAttemptService loginAttemptService;

  public LoginHandlers(LoginAttemptService loginAttemptService) {
    this.loginAttemptService = loginAttemptService;
  }

  public AuthenticationSuccessHandler successHandler() {
    return (request, response, authentication) -> {
      request.getSession().setAttribute(SessionAttributes.SSH_USERNAME, authentication.getName());
      request
          .getSession()
          .setAttribute(SessionAttributes.SSH_PASSWORD, authentication.getCredentials().toString());
      response.sendRedirect("/");
    };
  }

  public AuthenticationFailureHandler failureHandler() {
    return (request, response, exception) -> {
      String redirect = "/login?badCreds";
      if (exception instanceof LockedException) {
        redirect = "/login?locked";
      }
      response.sendRedirect(redirect);
    };
  }
}
