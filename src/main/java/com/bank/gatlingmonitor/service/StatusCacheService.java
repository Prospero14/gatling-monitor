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
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatusCacheService {

  private final GeneratorMonitorService generatorMonitorService;
  private final CredentialStoreService credentialStoreService;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final AtomicBoolean updating = new AtomicBoolean(false);
  private final AtomicInteger completedGenerators = new AtomicInteger(0);
  private final AtomicInteger totalGenerators = new AtomicInteger(0);

  private volatile List<GeneratorStatus> statuses = List.of();
  private volatile Instant checkedAt;

  public StatusCacheService(
      GeneratorMonitorService generatorMonitorService,
      CredentialStoreService credentialStoreService) {
    this.generatorMonitorService = generatorMonitorService;
    this.credentialStoreService = credentialStoreService;
  }

  public StatusSnapshot getSnapshot(boolean autoRefreshEnabled) {
    List<GeneratorStatus> current = statuses;
    int total = totalGenerators.get();
    int completed = completedGenerators.get();
    boolean isUpdating = updating.get();
    int progress = 0;
    if (isUpdating && total > 0) {
      progress = Math.min(99, (completed * 100) / total);
    } else if (!isUpdating && completed > 0 && completed >= total && total > 0) {
      progress = 100;
    }

    return new StatusSnapshot(
        current,
        checkedAt,
        isUpdating,
        autoRefreshEnabled,
        progress,
        completed,
        total,
        credentialStoreService.hasCredentials(),
        current.stream().filter(GeneratorStatus::isBusy).count(),
        current.stream().filter(status -> status.isReachable() && !status.isBusy()).count(),
        current.stream().filter(status -> !status.isReachable()).count());
  }

  public boolean startRefresh(SshCredentials credentials) {
    if (!updating.compareAndSet(false, true)) {
      return false;
    }

    int total = generatorMonitorService.getGeneratorCount();
    totalGenerators.set(total);
    completedGenerators.set(0);

    executor.submit(
        () -> {
          try {
            List<GeneratorStatus> fresh =
                generatorMonitorService.collectStatuses(
                    credentials, completed -> completedGenerators.set(completed));
            if (CredentialRejectionDetector.isCredentialsRejected(fresh)) {
              return;
            }
            statuses = fresh;
            checkedAt = Instant.now();
            credentialStoreService.save(credentials);
          } finally {
            completedGenerators.set(totalGenerators.get());
            updating.set(false);
          }
        });
    return true;
  }

  public boolean startAutoRefresh() {
    return credentialStoreService
        .get()
        .map(this::startRefresh)
        .orElse(false);
  }
}
