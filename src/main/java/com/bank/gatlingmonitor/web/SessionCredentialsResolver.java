package com.bank.gatlingmonitor.web;

import com.bank.gatlingmonitor.model.SshCredentials;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessionCredentialsResolver {

  public Optional<SshCredentials> resolve(HttpSession session) {
    Object username = session.getAttribute(SessionAttributes.SSH_USERNAME);
    Object password = session.getAttribute(SessionAttributes.SSH_PASSWORD);
    if (username == null || password == null) {
      return Optional.empty();
    }
    return Optional.of(new SshCredentials(username.toString(), password.toString()));
  }
}
