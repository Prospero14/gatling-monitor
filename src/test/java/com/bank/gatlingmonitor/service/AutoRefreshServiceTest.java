package com.bank.gatlingmonitor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoRefreshServiceTest {

  @Test
  void canBeEnabledAndDisabled() {
    StatusCacheService cacheService =
        new StatusCacheService(() -> java.util.List.of());
    AutoRefreshService autoRefreshService = new AutoRefreshService(cacheService);

    assertFalse(autoRefreshService.isEnabled());
    autoRefreshService.setEnabled(true);
    assertTrue(autoRefreshService.isEnabled());
    autoRefreshService.setEnabled(false);
    assertFalse(autoRefreshService.isEnabled());
  }
}
