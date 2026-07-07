package com.bank.gatlingmonitor.util;

public final class AuthErrors {

  private AuthErrors() {}

  public static boolean isAuthFailure(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      String message = current.getMessage();
      if (message != null) {
        String lower = message.toLowerCase();
        if (lower.contains("auth fail")
            || lower.contains("auth cancel")
            || lower.contains("userauth fail")
            || lower.contains("authentication failed")) {
          return true;
        }
      }
      current = current.getCause();
    }
    return false;
  }
}
