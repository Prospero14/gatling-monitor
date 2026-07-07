package com.bank.gatlingmonitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.gatlingmonitor.model.SshCredentials;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Map;

@Service
public class CredentialCryptoService {

  private final PrivateKey privateKey;
  private final String publicKeyBase64;
  private final ObjectMapper objectMapper;

  public CredentialCryptoService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      privateKey = keyPair.getPrivate();
      publicKeyBase64 =
          Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to initialize RSA keys", ex);
    }
  }

  public String getPublicKeyBase64() {
    return publicKeyBase64;
  }

  public SshCredentials decryptCredentials(String payloadBase64) {
    try {
      Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
      OAEPParameterSpec oaepParams =
          new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
      cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
      byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(payloadBase64));
      String json = new String(decrypted, StandardCharsets.UTF_8);
      Map<?, ?> map = objectMapper.readValue(json, Map.class);
      Object username = map.get("username");
      Object password = map.get("password");
      if (username == null || password == null) {
        throw new IllegalArgumentException("Invalid credentials payload");
      }
      return new SshCredentials(username.toString(), password.toString());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Failed to decrypt credentials", ex);
    }
  }
}
