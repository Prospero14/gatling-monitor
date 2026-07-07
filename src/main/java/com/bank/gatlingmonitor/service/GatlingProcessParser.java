package com.bank.gatlingmonitor.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GatlingProcessParser {

  private static final Pattern SIMULATION_CLASS =
      Pattern.compile("-Dgatling\\.simulationClass=([^\\s]+)");
  private static final String SIMULATIONS_PREFIX = "simulations.";

  public List<String> parse(String psOutput) {
    if (psOutput == null || psOutput.isBlank()) {
      return List.of();
    }

    Set<String> simulations = new LinkedHashSet<>();
    for (String line : psOutput.split("\\R")) {
      if (line.isBlank() || line.contains(" grep ")) {
        continue;
      }
      Matcher matcher = SIMULATION_CLASS.matcher(line);
      while (matcher.find()) {
        simulations.add(formatSimulation(matcher.group(1)));
      }
    }
    return new ArrayList<>(simulations);
  }

  private String formatSimulation(String rawValue) {
    if (rawValue.startsWith(SIMULATIONS_PREFIX)) {
      return "." + rawValue.substring(SIMULATIONS_PREFIX.length());
    }
    int lastDot = rawValue.lastIndexOf('.');
    if (lastDot >= 0) {
      return rawValue.substring(lastDot);
    }
    return rawValue;
  }
}
