package com.bank.gatlingmonitor.util;

import com.bank.gatlingmonitor.model.GeneratorStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialRejectionDetectorTest {

  @Test
  void detectsAuthRejectionOnAllGenerators() {
    List<GeneratorStatus> statuses =
        List.of(
            GeneratorStatus.authFailed("gen-1", "10.0.0.1", Instant.now()),
            GeneratorStatus.authFailed("gen-2", "10.0.0.2", Instant.now()));

    assertTrue(CredentialRejectionDetector.isCredentialsRejected(statuses));
  }

  @Test
  void doesNotTreatPartialAuthFailuresAsGlobalRejection() {
    List<GeneratorStatus> statuses =
        List.of(
            GeneratorStatus.authFailed("gen-1", "10.0.0.1", Instant.now()),
            GeneratorStatus.ok("gen-2", "10.0.0.2", List.of(), Instant.now()));

    assertFalse(CredentialRejectionDetector.isCredentialsRejected(statuses));
  }

  @Test
  void doesNotTreatNetworkErrorsAsCredentialRejection() {
    List<GeneratorStatus> statuses =
        List.of(
            GeneratorStatus.unreachable("gen-1", "10.0.0.1", "Auth fail", Instant.now()),
            GeneratorStatus.unreachable("gen-2", "10.0.0.2", "Connection timed out", Instant.now()));

    assertFalse(CredentialRejectionDetector.isCredentialsRejected(statuses));
  }
}
