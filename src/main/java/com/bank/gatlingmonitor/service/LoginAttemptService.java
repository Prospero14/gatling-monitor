package com.bank.gatlingmonitor.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

  private static final int MAX_ATTEMPTS = 2;
  private static final Duration LOCK_DURATION = Duration.ofMinutes(1);

  private final ConcurrentHashMap<String, AttemptRecord> records = new ConcurrentHashMap<>();

  public boolean isBlocked(String clientKey) {
    AttemptRecord record = records.get(clientKey);
    if (record == null) {
      return false;
    }
    if (record.lockedUntil == null) {
      return false;
    }
    if (Instant.now().isBefore(record.lockedUntil)) {
      return true;
    }
    records.remove(clientKey);
    return false;
  }

  public long getRemainingLockSeconds(String clientKey) {
    AttemptRecord record = records.get(clientKey);
    if (record == null || record.lockedUntil == null) {
      return 0;
    }
    long seconds = Duration.between(Instant.now(), record.lockedUntil).toSeconds();
    return Math.max(seconds, 0);
  }

  public int getRemainingAttempts(String clientKey) {
    if (isBlocked(clientKey)) {
      return 0;
    }
    AttemptRecord record = records.get(clientKey);
    if (record == null) {
      return MAX_ATTEMPTS;
    }
    return Math.max(MAX_ATTEMPTS - record.attempts, 0);
  }

  public void loginFailed(String clientKey) {
    AttemptRecord record = records.computeIfAbsent(clientKey, key -> new AttemptRecord());
    record.attempts++;
    if (record.attempts >= MAX_ATTEMPTS) {
      record.lockedUntil = Instant.now().plus(LOCK_DURATION);
    }
  }

  public void loginSucceeded(String clientKey) {
    records.remove(clientKey);
  }

  private static final class AttemptRecord {
    private int attempts;
    private Instant lockedUntil;
  }
}
