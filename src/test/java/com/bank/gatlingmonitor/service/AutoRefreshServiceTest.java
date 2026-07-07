package com.bank.gatlingmonitor.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AutoRefreshServiceTest {

  @Test
  void canBeEnabledAndDisabled() {
    GeneratorMonitorService monitorService = mock(GeneratorMonitorService.class);
    CredentialStoreService credentialStoreService = new CredentialStoreService();
    StatusCacheService cacheService =
        new StatusCacheService(monitorService, credentialStoreService);
    AutoRefreshService autoRefreshService = new AutoRefreshService(cacheService);

    assertFalse(autoRefreshService.isEnabled());
    autoRefreshService.setEnabled(true);
    assertTrue(autoRefreshService.isEnabled());
    autoRefreshService.setEnabled(false);
    assertFalse(autoRefreshService.isEnabled());
  }
}
