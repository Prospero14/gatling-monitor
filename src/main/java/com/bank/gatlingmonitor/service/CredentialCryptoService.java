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
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Map;

@Service
public class CredentialCryptoService {

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final String publicKeyBase64;
  private final ObjectMapper objectMapper;

  public CredentialCryptoService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      privateKey = keyPair.getPrivate();
      publicKey = keyPair.getPublic();
      publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to initialize RSA keys", ex);
    }
  }

  public String getPublicKeyBase64() {
    return publicKeyBase64;
  }

  public String getPublicKeyPem() {
    String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    StringBuilder pem = new StringBuilder("-----BEGIN PUBLIC KEY-----\n");
    for (int i = 0; i < base64.length(); i += 64) {
      pem.append(base64, i, Math.min(i + 64, base64.length())).append('\n');
    }
    pem.append("-----END PUBLIC KEY-----");
    return pem.toString();
  }

  public SshCredentials decryptCredentials(
      String payloadBase64, String usernamePayloadBase64, String passwordPayloadBase64) {
    if (usernamePayloadBase64 != null
        && !usernamePayloadBase64.isBlank()
        && passwordPayloadBase64 != null
        && !passwordPayloadBase64.isBlank()) {
      String username = decryptPkcs1(usernamePayloadBase64);
      String password = decryptPkcs1(passwordPayloadBase64);
      return new SshCredentials(username, password);
    }

    if (payloadBase64 == null || payloadBase64.isBlank()) {
      throw new IllegalArgumentException("Encrypted payload is empty");
    }

    return decryptJsonPayload(payloadBase64);
  }

  private SshCredentials decryptJsonPayload(String payloadBase64) {
    try {
      String json = decryptOaep(payloadBase64);
      return parseCredentials(json);
    } catch (Exception oaepError) {
      try {
        String json = decryptPkcs1(payloadBase64);
        return parseCredentials(json);
      } catch (Exception pkcsError) {
        throw new IllegalArgumentException("Failed to decrypt credentials", pkcsError);
      }
    }
  }

  private SshCredentials parseCredentials(String json) {
    try {
      Map<?, ?> map = objectMapper.readValue(json, Map.class);
      Object username = map.get("username");
      Object password = map.get("password");
      if (username == null || password == null) {
        throw new IllegalArgumentException("Invalid credentials payload");
      }
      return new SshCredentials(username.toString(), password.toString());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid credentials payload", ex);
    }
  }

  private String decryptOaep(String payloadBase64) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    OAEPParameterSpec oaepParams =
        new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
    byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(payloadBase64));
    return new String(decrypted, StandardCharsets.UTF_8);
  }

  private String decryptPkcs1(String payloadBase64) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(payloadBase64));
    return new String(decrypted, StandardCharsets.UTF_8);
  }
}
