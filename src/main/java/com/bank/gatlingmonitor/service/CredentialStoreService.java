package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.model.SshCredentials;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CredentialStoreService {

  private volatile SshCredentials lastSuccessfulCredentials;

  public void save(SshCredentials credentials) {
    lastSuccessfulCredentials = credentials;
  }

  public Optional<SshCredentials> get() {
    return Optional.ofNullable(lastSuccessfulCredentials);
  }

  public boolean hasCredentials() {
    return lastSuccessfulCredentials != null;
  }
}
