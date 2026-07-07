package com.bank.gatlingmonitor.model;

import java.time.Instant;
import java.util.List;

public class GeneratorStatus {

  private final String name;
  private final String host;
  private final boolean reachable;
  private final boolean busy;
  private final boolean authFailed;
  private final List<String> simulations;
  private final String error;
  private final Instant checkedAt;

  private GeneratorStatus(
      String name,
      String host,
      boolean reachable,
      boolean busy,
      boolean authFailed,
      List<String> simulations,
      String error,
      Instant checkedAt) {
    this.name = name;
    this.host = host;
    this.reachable = reachable;
    this.busy = busy;
    this.authFailed = authFailed;
    this.simulations = List.copyOf(simulations);
    this.error = error;
    this.checkedAt = checkedAt;
  }

  public static GeneratorStatus ok(
      String name, String host, List<String> simulations, Instant checkedAt) {
    return new GeneratorStatus(
        name, host, true, !simulations.isEmpty(), false, simulations, null, checkedAt);
  }

  public static GeneratorStatus authFailed(String name, String host, Instant checkedAt) {
    return new GeneratorStatus(name, host, false, false, true, List.of(), null, checkedAt);
  }

  public static GeneratorStatus unreachable(
      String name, String host, String error, Instant checkedAt) {
    return new GeneratorStatus(name, host, false, false, false, List.of(), error, checkedAt);
  }

  public String getName() {
    return name;
  }

  public String getHost() {
    return host;
  }

  public boolean isReachable() {
    return reachable;
  }

  public boolean isBusy() {
    return busy;
  }

  public boolean isAuthFailed() {
    return authFailed;
  }

  public List<String> getSimulations() {
    return simulations;
  }

  public String getError() {
    return error;
  }

  public Instant getCheckedAt() {
    return checkedAt;
  }

  public String getSimulationsText() {
    if (simulations.isEmpty()) {
      return "—";
    }
    return String.join(", ", simulations);
  }

  public String getStatusLabel() {
    if (authFailed) {
      return "не удалось авторизоваться";
    }
    if (!reachable) {
      return "НЕДОСТУПЕН";
    }
    return busy ? "ЗАНЯТ" : "СВОБОДЕН";
  }

  public String getStatusClass() {
    if (authFailed) {
      return "status-auth-failed";
    }
    if (!reachable) {
      return "status-offline";
    }
    return busy ? "status-busy" : "status-free";
  }
}
