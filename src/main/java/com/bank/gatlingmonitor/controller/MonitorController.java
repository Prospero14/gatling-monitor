package com.bank.gatlingmonitor.controller;

import com.bank.gatlingmonitor.model.StatusSnapshot;
import com.bank.gatlingmonitor.service.AutoRefreshService;
import com.bank.gatlingmonitor.service.StatusCacheService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MonitorController {

  private final StatusCacheService statusCacheService;
  private final AutoRefreshService autoRefreshService;

  public MonitorController(
      StatusCacheService statusCacheService, AutoRefreshService autoRefreshService) {
    this.statusCacheService = statusCacheService;
    this.autoRefreshService = autoRefreshService;
  }

  @GetMapping("/")
  public String index(Model model) {
    fillModel(model);
    return "index";
  }

  @PostMapping("/refresh")
  public String refresh(RedirectAttributes redirectAttributes) {
    boolean started = statusCacheService.startRefresh();
    if (!started) {
      redirectAttributes.addFlashAttribute("alreadyUpdating", true);
    }
    return "redirect:/";
  }

  @PostMapping("/api/refresh")
  @ResponseBody
  public Map<String, Object> refreshApi() {
    Map<String, Object> body = new HashMap<>();
    body.put("started", statusCacheService.startRefresh());
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
    model.addAttribute("busyCount", snapshot.busyCount());
    model.addAttribute("freeCount", snapshot.freeCount());
    model.addAttribute("offlineCount", snapshot.offlineCount());
  }
}
