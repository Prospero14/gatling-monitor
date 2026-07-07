package com.bank.gatlingmonitor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

  private final LoginAttemptService service = new LoginAttemptService();

  @Test
  void blocksAfterTwoFailedAttempts() {
    String client = "10.0.0.1";

    assertFalse(service.isBlocked(client));
    service.loginFailed(client);
    assertFalse(service.isBlocked(client));
    assertEquals(1, service.getRemainingAttempts(client));

    service.loginFailed(client);
    assertTrue(service.isBlocked(client));
    assertEquals(0, service.getRemainingAttempts(client));
    assertTrue(service.getRemainingLockSeconds(client) > 0);
  }

  @Test
  void resetsAttemptsAfterSuccess() {
    String client = "10.0.0.2";
    service.loginFailed(client);
    service.loginSucceeded(client);

    assertEquals(2, service.getRemainingAttempts(client));
    assertFalse(service.isBlocked(client));
  }
}
