package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.model.GeneratorStatus;
import com.bank.gatlingmonitor.model.SshCredentials;
import com.bank.gatlingmonitor.model.StatusSnapshot;
import com.bank.gatlingmonitor.util.CredentialRejectionDetector;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class StatusCacheService {

  public static final String CREDENTIALS_POPUP_MESSAGE = "эх, ты, простофиля, креды проверь!";

  private final GeneratorMonitorService generatorMonitorService;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final AtomicBoolean updating = new AtomicBoolean(false);
  private final AtomicBoolean credentialsRejected = new AtomicBoolean(false);

  private volatile List<GeneratorStatus> statuses = List.of();
  private volatile Instant checkedAt;
  private volatile SshCredentials lastSuccessfulCredentials;

  public StatusCacheService(GeneratorMonitorService generatorMonitorService) {
    this.generatorMonitorService = generatorMonitorService;
  }

  public StatusSnapshot getSnapshot(boolean autoRefreshEnabled) {
    List<GeneratorStatus> current = statuses;
    return new StatusSnapshot(
        current,
        checkedAt,
        updating.get(),
        credentialsRejected.getAndSet(false),
        autoRefreshEnabled,
        lastSuccessfulCredentials != null,
        current.stream().filter(GeneratorStatus::isBusy).count(),
        current.stream().filter(status -> status.isReachable() && !status.isBusy()).count(),
        current.stream().filter(status -> !status.isReachable()).count());
  }

  public boolean startManualRefresh(SshCredentials credentials) {
    return startRefresh(credentials, true);
  }

  public boolean startAutoRefresh() {
    SshCredentials credentials = lastSuccessfulCredentials;
    if (credentials == null) {
      return false;
    }
    return startRefresh(credentials, false);
  }

  public boolean hasLastSuccessfulCredentials() {
    return lastSuccessfulCredentials != null;
  }

  private boolean startRefresh(SshCredentials credentials, boolean notifyOnCredentialRejection) {
    if (!updating.compareAndSet(false, true)) {
      return false;
    }

    executor.submit(
        () -> {
          try {
            List<GeneratorStatus> fresh = generatorMonitorService.collectStatuses(credentials);
            if (CredentialRejectionDetector.isCredentialsRejected(fresh)) {
              if (notifyOnCredentialRejection) {
                credentialsRejected.set(true);
              }
              return;
            }
            statuses = fresh;
            checkedAt = Instant.now();
            lastSuccessfulCredentials = credentials;
            credentialsRejected.set(false);
          } finally {
            updating.set(false);
          }
        });
    return true;
  }
}
