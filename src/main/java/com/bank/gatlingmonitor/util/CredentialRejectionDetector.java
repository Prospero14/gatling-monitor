package com.bank.gatlingmonitor.util;

import com.bank.gatlingmonitor.model.GeneratorStatus;

import java.util.List;

public final class CredentialRejectionDetector {

  private CredentialRejectionDetector() {}

  public static boolean isCredentialsRejected(List<GeneratorStatus> statuses) {
    if (statuses.isEmpty()) {
      return false;
    }
    return statuses.stream().allMatch(CredentialRejectionDetector::isAuthFailure);
  }

  private static boolean isAuthFailure(GeneratorStatus status) {
    if (status.isAuthFailed()) {
      return true;
    }
    return !status.isReachable()
        && status.getError() != null
        && AuthErrors.isAuthFailure(new Exception(status.getError()));
  }
}
