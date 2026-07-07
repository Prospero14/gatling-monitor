package com.bank.gatlingmonitor.config;

import com.bank.gatlingmonitor.security.LoginHandlers;
import com.bank.gatlingmonitor.security.SshAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  private final SshAuthenticationProvider sshAuthenticationProvider;
  private final LoginHandlers loginHandlers;

  public SecurityConfig(
      SshAuthenticationProvider sshAuthenticationProvider, LoginHandlers loginHandlers) {
    this.sshAuthenticationProvider = sshAuthenticationProvider;
    this.loginHandlers = loginHandlers;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/login", "/css/**", "/img/**", "/preview.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(loginHandlers.successHandler())
                    .failureHandler(loginHandlers.failureHandler())
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID"))
        .authenticationProvider(sshAuthenticationProvider);
    return http.build();
  }
}
