package com.bank.gatlingmonitor.model;

public class SshCredentials {

  private final String username;
  private final String password;

  public SshCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
