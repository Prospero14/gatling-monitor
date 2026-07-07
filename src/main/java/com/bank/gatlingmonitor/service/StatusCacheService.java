package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.model.GeneratorStatus;
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

  private final GeneratorMonitorService generatorMonitorService;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final AtomicBoolean updating = new AtomicBoolean(false);

  private volatile List<GeneratorStatus> statuses = List.of();
  private volatile Instant checkedAt;

  public StatusCacheService(GeneratorMonitorService generatorMonitorService) {
    this.generatorMonitorService = generatorMonitorService;
  }

  public StatusSnapshot getSnapshot(boolean autoRefreshEnabled) {
    List<GeneratorStatus> current = statuses;
    return new StatusSnapshot(
        current,
        checkedAt,
        updating.get(),
        autoRefreshEnabled,
        current.stream().filter(GeneratorStatus::isBusy).count(),
        current.stream().filter(status -> status.isReachable() && !status.isBusy()).count(),
        current.stream().filter(status -> !status.isReachable()).count());
  }

  public boolean startRefresh() {
    if (!updating.compareAndSet(false, true)) {
      return false;
    }

    executor.submit(
        () -> {
          try {
            List<GeneratorStatus> fresh = generatorMonitorService.collectStatuses();
            if (CredentialRejectionDetector.isCredentialsRejected(fresh)) {
              return;
            }
            statuses = fresh;
            checkedAt = Instant.now();
          } finally {
            updating.set(false);
          }
        });
    return true;
  }
}
