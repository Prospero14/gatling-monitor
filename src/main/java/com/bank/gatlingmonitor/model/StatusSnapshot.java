package com.bank.gatlingmonitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class StatusSnapshot {

  private final List<GeneratorStatus> statuses;
  private final Instant checkedAt;
  private final boolean updating;
  private final boolean autoRefreshEnabled;
  private final int progressPercent;
  private final int completedGenerators;
  private final int totalGenerators;
  private final boolean hasLastCredentials;
  private final long busyCount;
  private final long freeCount;
  private final long offlineCount;

  public StatusSnapshot(
      List<GeneratorStatus> statuses,
      Instant checkedAt,
      boolean updating,
      boolean autoRefreshEnabled,
      int progressPercent,
      int completedGenerators,
      int totalGenerators,
      boolean hasLastCredentials,
      long busyCount,
      long freeCount,
      long offlineCount) {
    this.statuses = statuses;
    this.checkedAt = checkedAt;
    this.updating = updating;
    this.autoRefreshEnabled = autoRefreshEnabled;
    this.progressPercent = progressPercent;
    this.completedGenerators = completedGenerators;
    this.totalGenerators = totalGenerators;
    this.hasLastCredentials = hasLastCredentials;
    this.busyCount = busyCount;
    this.freeCount = freeCount;
    this.offlineCount = offlineCount;
  }

  public List<GeneratorStatus> getStatuses() {
    return statuses;
  }

  public Instant getCheckedAt() {
    return checkedAt;
  }

  @JsonProperty("updating")
  public boolean isUpdating() {
    return updating;
  }

  public boolean isAutoRefreshEnabled() {
    return autoRefreshEnabled;
  }

  public int getProgressPercent() {
    return progressPercent;
  }

  public int getCompletedGenerators() {
    return completedGenerators;
  }

  public int getTotalGenerators() {
    return totalGenerators;
  }

  public boolean isHasLastCredentials() {
    return hasLastCredentials;
  }

  public long getBusyCount() {
    return busyCount;
  }

  public long getFreeCount() {
    return freeCount;
  }

  public long getOfflineCount() {
    return offlineCount;
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

  public int progressPercent() {
    return progressPercent;
  }

  public boolean hasLastCredentials() {
    return hasLastCredentials;
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
