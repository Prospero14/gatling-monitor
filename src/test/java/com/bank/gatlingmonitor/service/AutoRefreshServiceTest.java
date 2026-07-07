package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.model.SshCredentials;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoRefreshServiceTest {

  @Test
  void canBeEnabledAndDisabled() {
    StatusCacheService cacheService =
        new StatusCacheService(credentials -> java.util.List.of());
    AutoRefreshService autoRefreshService = new AutoRefreshService(cacheService);

    assertFalse(autoRefreshService.isEnabled());
    autoRefreshService.setEnabled(true);
    assertTrue(autoRefreshService.isEnabled());
    autoRefreshService.setEnabled(false);
    assertFalse(autoRefreshService.isEnabled());
  }

  @Test
  void autoRefreshWithoutSavedCredentialsDoesNotStart() {
    StatusCacheService cacheService =
        new StatusCacheService(credentials -> java.util.List.of());
    AutoRefreshService autoRefreshService = new AutoRefreshService(cacheService);

    autoRefreshService.setEnabled(true);
    assertFalse(cacheService.startAutoRefresh());
  }
}
