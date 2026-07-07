package com.bank.gatlingmonitor.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {

  @Valid
  private Ssh ssh = new Ssh();

  @NotEmpty
  @Valid
  private List<Generator> generators;

  public Ssh getSsh() {
    return ssh;
  }

  public void setSsh(Ssh ssh) {
    this.ssh = ssh;
  }

  public List<Generator> getGenerators() {
    return generators;
  }

  public void setGenerators(List<Generator> generators) {
    this.generators = generators;
  }

  public static class Ssh {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Positive
    private int port = 22;

    @Positive
    private int connectTimeoutMs = 10_000;

    @Positive
    private int commandTimeoutMs = 15_000;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public int getConnectTimeoutMs() {
      return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
      this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getCommandTimeoutMs() {
      return commandTimeoutMs;
    }

    public void setCommandTimeoutMs(int commandTimeoutMs) {
      this.commandTimeoutMs = commandTimeoutMs;
    }
  }

  public static class Generator {

    @NotBlank
    private String name;

    @NotBlank
    private String host;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }
  }
}
