package com.bank.gatlingmonitor.model;

import java.time.Instant;
import java.util.List;

public record StatusSnapshot(
    List<GeneratorStatus> statuses,
    Instant checkedAt,
    boolean updating,
    boolean credentialsRejected,
    boolean autoRefreshEnabled,
    boolean hasLastCredentials,
    long busyCount,
    long freeCount,
    long offlineCount) {}
