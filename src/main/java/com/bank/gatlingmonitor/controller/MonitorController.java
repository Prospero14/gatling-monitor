package com.bank.gatlingmonitor.controller;

import com.bank.gatlingmonitor.model.EncryptedPayloadRequest;
import com.bank.gatlingmonitor.model.SshCredentials;
import com.bank.gatlingmonitor.model.StatusSnapshot;
import com.bank.gatlingmonitor.service.AutoRefreshService;
import com.bank.gatlingmonitor.service.CredentialCryptoService;
import com.bank.gatlingmonitor.service.SshConnectionService;
import com.bank.gatlingmonitor.service.StatusCacheService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MonitorController {

  public static final String AUTH_ERROR_MESSAGE = "эх, ты, простофиля, креды проверь!";

  private final StatusCacheService statusCacheService;
  private final AutoRefreshService autoRefreshService;
  private final CredentialCryptoService credentialCryptoService;
  private final SshConnectionService sshConnectionService;

  public MonitorController(
      StatusCacheService statusCacheService,
      AutoRefreshService autoRefreshService,
      CredentialCryptoService credentialCryptoService,
      SshConnectionService sshConnectionService) {
    this.statusCacheService = statusCacheService;
    this.autoRefreshService = autoRefreshService;
    this.credentialCryptoService = credentialCryptoService;
    this.sshConnectionService = sshConnectionService;
  }

  @GetMapping("/")
  public String index(Model model) {
    fillModel(model);
    return "index";
  }

  @GetMapping("/api/auth/public-key")
  @ResponseBody
  public Map<String, String> publicKey() {
    Map<String, String> body = new HashMap<>();
    body.put("publicKey", credentialCryptoService.getPublicKeyBase64());
    return body;
  }

  @PostMapping("/api/refresh")
  @ResponseBody
  public Map<String, Object> refreshApi(@RequestBody EncryptedPayloadRequest request) {
    Map<String, Object> body = new HashMap<>();
    if (request == null || request.getPayload() == null || request.getPayload().isBlank()) {
      body.put("started", false);
      body.put("authRequired", true);
      return body;
    }

    SshCredentials credentials;
    try {
      credentials = credentialCryptoService.decryptCredentials(request.getPayload());
    } catch (IllegalArgumentException ex) {
      body.put("started", false);
      body.put("authFailed", true);
      body.put("message", AUTH_ERROR_MESSAGE);
      return body;
    }

    if (!sshConnectionService.canAuthenticateAny(credentials)) {
      body.put("started", false);
      body.put("authFailed", true);
      body.put("message", AUTH_ERROR_MESSAGE);
      return body;
    }

    boolean started = statusCacheService.startRefresh(credentials);
    body.put("started", started);
    if (!started) {
      body.put("alreadyUpdating", true);
    }
    return body;
  }

  @PostMapping("/auto-refresh")
  public String toggleAutoRefresh(@RequestParam("enabled") boolean enabled) {
    autoRefreshService.setEnabled(enabled);
    return "redirect:/";
  }

  @GetMapping("/api/status")
  @ResponseBody
  public StatusSnapshot status() {
    return statusCacheService.getSnapshot(autoRefreshService.isEnabled());
  }

  private void fillModel(Model model) {
    StatusSnapshot snapshot = statusCacheService.getSnapshot(autoRefreshService.isEnabled());
    model.addAttribute("statuses", snapshot.statuses());
    model.addAttribute("checkedAt", snapshot.checkedAt());
    model.addAttribute("updating", snapshot.updating());
    model.addAttribute("progressPercent", snapshot.progressPercent());
    model.addAttribute("autoRefreshEnabled", snapshot.autoRefreshEnabled());
    model.addAttribute("hasLastCredentials", snapshot.hasLastCredentials());
    model.addAttribute("authErrorMessage", AUTH_ERROR_MESSAGE);
    model.addAttribute("busyCount", snapshot.busyCount());
    model.addAttribute("freeCount", snapshot.freeCount());
    model.addAttribute("offlineCount", snapshot.offlineCount());
  }
}
