package com.bank.gatlingmonitor.model;

import java.time.Instant;
import java.util.List;

public class StatusSnapshot {

  private final List<GeneratorStatus> statuses;
  private final Instant checkedAt;
  private final boolean updating;
  private final boolean autoRefreshEnabled;
  private final long busyCount;
  private final long freeCount;
  private final long offlineCount;

  public StatusSnapshot(
      List<GeneratorStatus> statuses,
      Instant checkedAt,
      boolean updating,
      boolean autoRefreshEnabled,
      long busyCount,
      long freeCount,
      long offlineCount) {
    this.statuses = statuses;
    this.checkedAt = checkedAt;
    this.updating = updating;
    this.autoRefreshEnabled = autoRefreshEnabled;
    this.busyCount = busyCount;
    this.freeCount = freeCount;
    this.offlineCount = offlineCount;
  }

  public List<GeneratorStatus> statuses() {
    return statuses;
  }

  public Instant checkedAt() {
    return checkedAt;
  }

  public boolean updating() {
    return updating;
  }

  public boolean autoRefreshEnabled() {
    return autoRefreshEnabled;
  }

  public long busyCount() {
    return busyCount;
  }

  public long freeCount() {
    return freeCount;
  }

  public long offlineCount() {
    return offlineCount;
  }
}
