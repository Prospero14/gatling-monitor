package com.bank.gatlingmonitor.controller;

import com.bank.gatlingmonitor.model.SshCredentials;
import com.bank.gatlingmonitor.model.StatusSnapshot;
import com.bank.gatlingmonitor.service.AutoRefreshService;
import com.bank.gatlingmonitor.service.LoginAttemptService;
import com.bank.gatlingmonitor.service.StatusCacheService;
import com.bank.gatlingmonitor.web.ClientIpResolver;
import com.bank.gatlingmonitor.web.SessionCredentialsResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class MonitorController {

  private final StatusCacheService statusCacheService;
  private final AutoRefreshService autoRefreshService;
  private final SessionCredentialsResolver sessionCredentialsResolver;
  private final LoginAttemptService loginAttemptService;

  public MonitorController(
      StatusCacheService statusCacheService,
      AutoRefreshService autoRefreshService,
      SessionCredentialsResolver sessionCredentialsResolver,
      LoginAttemptService loginAttemptService) {
    this.statusCacheService = statusCacheService;
    this.autoRefreshService = autoRefreshService;
    this.sessionCredentialsResolver = sessionCredentialsResolver;
    this.loginAttemptService = loginAttemptService;
  }

  @GetMapping("/login")
  public String login(
      @RequestParam(name = "badCreds", required = false) String badCreds,
      @RequestParam(name = "locked", required = false) String locked,
      @RequestParam(name = "logout", required = false) String logout,
      HttpServletRequest request,
      Model model) {
    String clientKey = ClientIpResolver.resolve(request);
    model.addAttribute("blocked", loginAttemptService.isBlocked(clientKey));
    model.addAttribute("retryAfterSeconds", loginAttemptService.getRemainingLockSeconds(clientKey));
    model.addAttribute("remainingAttempts", loginAttemptService.getRemainingAttempts(clientKey));
    model.addAttribute("badCreds", badCreds != null);
    model.addAttribute("locked", locked != null);
    model.addAttribute("logout", logout != null);
    model.addAttribute("popupMessage", StatusCacheService.CREDENTIALS_POPUP_MESSAGE);
    return "login";
  }

  @GetMapping("/")
  public String index(Model model) {
    fillModel(model);
    return "index";
  }

  @PostMapping("/refresh")
  public String refresh(HttpSession session, RedirectAttributes redirectAttributes) {
    Optional<SshCredentials> credentials = sessionCredentialsResolver.resolve(session);
    if (credentials.isEmpty()) {
      return "redirect:/login";
    }

    boolean started = statusCacheService.startManualRefresh(credentials.get());
    if (!started) {
      redirectAttributes.addFlashAttribute("alreadyUpdating", true);
    }
    return "redirect:/";
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
    model.addAttribute("autoRefreshEnabled", snapshot.autoRefreshEnabled());
    model.addAttribute("hasLastCredentials", snapshot.hasLastCredentials());
    model.addAttribute("popupMessage", StatusCacheService.CREDENTIALS_POPUP_MESSAGE);
    model.addAttribute("busyCount", snapshot.busyCount());
    model.addAttribute("freeCount", snapshot.freeCount());
    model.addAttribute("offlineCount", snapshot.offlineCount());
  }
}
