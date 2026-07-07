package com.bank.gatlingmonitor.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AutoRefreshService {

  private static final long TWO_HOURS_MS = 2 * 60 * 60 * 1000L;

  private final StatusCacheService statusCacheService;
  private final AtomicBoolean enabled = new AtomicBoolean(false);

  public AutoRefreshService(StatusCacheService statusCacheService) {
    this.statusCacheService = statusCacheService;
  }

  public boolean isEnabled() {
    return enabled.get();
  }

  public void setEnabled(boolean value) {
    enabled.set(value);
  }

  @Scheduled(fixedRate = TWO_HOURS_MS, initialDelay = TWO_HOURS_MS)
  public void refreshOnSchedule() {
    if (!enabled.get()) {
      return;
    }
    statusCacheService.startAutoRefresh();
  }
}
