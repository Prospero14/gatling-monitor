package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.config.MonitorProperties;
import com.bank.gatlingmonitor.model.SshCredentials;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
public class SshConnectionService {

  private static final String PS_COMMAND =
      "ps aux | grep java | grep -v grep; ps aux | grep '\\.sh' | grep -v grep";

  private final MonitorProperties monitorProperties;

  public SshConnectionService(MonitorProperties monitorProperties) {
    this.monitorProperties = monitorProperties;
  }

  public boolean canAuthenticate(String host, SshCredentials credentials) {
    Session session = null;
    try {
      session = openSession(host, credentials);
      session.connect(monitorProperties.getSsh().getConnectTimeoutMs());
      return session.isConnected();
    } catch (JSchException ex) {
      return false;
    } finally {
      if (session != null) {
        session.disconnect();
      }
    }
  }

  public boolean canAuthenticateAny(List<String> hosts, SshCredentials credentials) {
    return hosts.stream().anyMatch(host -> canAuthenticate(host, credentials));
  }

  public String execute(String host, SshCredentials credentials)
      throws JSchException, IOException, InterruptedException {
    Session session = openSession(host, credentials);
    session.connect(monitorProperties.getSsh().getConnectTimeoutMs());

    try {
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand(PS_COMMAND);
      channel.setInputStream(null);

      InputStream inputStream = channel.getInputStream();
      channel.connect(monitorProperties.getSsh().getCommandTimeoutMs());

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      long deadline = System.currentTimeMillis() + monitorProperties.getSsh().getCommandTimeoutMs();

      while (true) {
        while (inputStream.available() > 0) {
          int read = inputStream.read(buffer);
          if (read < 0) {
            break;
          }
          output.write(buffer, 0, read);
        }
        if (channel.isClosed()) {
          break;
        }
        if (System.currentTimeMillis() > deadline) {
          throw new IOException("SSH command timeout on host " + host);
        }
        Thread.sleep(Duration.ofMillis(100).toMillis());
      }

      return output.toString(StandardCharsets.UTF_8);
    } finally {
      session.disconnect();
    }
  }

  private Session openSession(String host, SshCredentials credentials) throws JSchException {
    JSch jsch = new JSch();
    MonitorProperties.Ssh ssh = monitorProperties.getSsh();
    Session session = jsch.getSession(credentials.username(), host, ssh.getPort());
    session.setPassword(credentials.password());
    session.setConfig("StrictHostKeyChecking", "no");
    return session;
  }
}
