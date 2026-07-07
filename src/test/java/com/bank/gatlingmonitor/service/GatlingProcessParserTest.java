package com.bank.gatlingmonitor.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatlingProcessParserTest {

  private final GatlingProcessParser parser = new GatlingProcessParser();

  @Test
  void parsesSimulationClassFromJavaProcess() {
    String psOutput =
        "loaduser  12345  1.0  5.0  java -Dgatling.simulationClass=simulations.efsFinmonMob.FinmonmobSimulationSTAB -jar gatling.jar\n"
            + "loaduser  99999  0.0  0.0  grep java\n";

    List<String> result = parser.parse(psOutput);

    assertEquals(1, result.size());
    assertEquals(".efsFinmonMob.FinmonmobSimulationSTAB", result.get(0));
  }

  @Test
  void parsesMultipleSimulations() {
    String psOutput =
        "user  1  java -Dgatling.simulationClass=simulations.pkg.FirstSimulation\n"
            + "user  2  java -Dgatling.simulationClass=simulations.pkg.SecondSimulation\n";

    List<String> result = parser.parse(psOutput);

    assertEquals(2, result.size());
    assertEquals(".pkg.FirstSimulation", result.get(0));
    assertEquals(".pkg.SecondSimulation", result.get(1));
  }

  @Test
  void returnsEmptyForNoGatlingProcesses() {
    assertTrue(parser.parse("user 1 bash run.sh").isEmpty());
    assertTrue(parser.parse("").isEmpty());
  }
}
