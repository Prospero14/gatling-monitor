package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.config.MonitorProperties;
import com.bank.gatlingmonitor.model.GeneratorStatus;
import com.bank.gatlingmonitor.model.SshCredentials;
import com.bank.gatlingmonitor.util.AuthErrors;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

@Service
public class GeneratorMonitorService {

  private final MonitorProperties monitorProperties;
  private final SshConnectionService sshConnectionService;
  private final GatlingProcessParser gatlingProcessParser;

  public GeneratorMonitorService(
      MonitorProperties monitorProperties,
      SshConnectionService sshConnectionService,
      GatlingProcessParser gatlingProcessParser) {
    this.monitorProperties = monitorProperties;
    this.sshConnectionService = sshConnectionService;
    this.gatlingProcessParser = gatlingProcessParser;
  }

  public int getGeneratorCount() {
    return monitorProperties.getGenerators().size();
  }

  public List<GeneratorStatus> collectStatuses(SshCredentials credentials) {
    return collectStatuses(credentials, completed -> {});
  }

  public List<GeneratorStatus> collectStatuses(
      SshCredentials credentials, IntConsumer onGeneratorCompleted) {
    List<GeneratorStatus> statuses = new ArrayList<>();
    for (MonitorProperties.Generator generator : monitorProperties.getGenerators()) {
      statuses.add(checkGenerator(generator, credentials));
      onGeneratorCompleted.accept(statuses.size());
    }
    return statuses;
  }

  private GeneratorStatus checkGenerator(
      MonitorProperties.Generator generator, SshCredentials credentials) {
    Instant checkedAt = Instant.now();
    try {
      String psOutput = sshConnectionService.execute(generator.getHost(), credentials);
      List<String> simulations = gatlingProcessParser.parse(psOutput);
      return GeneratorStatus.ok(generator.getName(), generator.getHost(), simulations, checkedAt);
    } catch (Exception ex) {
      if (AuthErrors.isAuthFailure(ex)) {
        return GeneratorStatus.authFailed(generator.getName(), generator.getHost(), checkedAt);
      }
      return GeneratorStatus.unreachable(
          generator.getName(), generator.getHost(), ex.getMessage(), checkedAt);
    }
  }
}
