package com.bank.gatlingmonitor.model;

public class EncryptedPayloadRequest {

  private String payload;
  private String usernamePayload;
  private String passwordPayload;

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getUsernamePayload() {
    return usernamePayload;
  }

  public void setUsernamePayload(String usernamePayload) {
    this.usernamePayload = usernamePayload;
  }

  public String getPasswordPayload() {
    return passwordPayload;
  }

  public void setPasswordPayload(String passwordPayload) {
    this.passwordPayload = passwordPayload;
  }
}
