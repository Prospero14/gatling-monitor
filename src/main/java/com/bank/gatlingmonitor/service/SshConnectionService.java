package com.bank.gatlingmonitor.service;

import com.bank.gatlingmonitor.config.MonitorProperties;
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

@Service
public class SshConnectionService {

  private static final String PS_COMMAND =
      "ps aux | grep java | grep -v grep; ps aux | grep '\\.sh' | grep -v grep";

  private final MonitorProperties monitorProperties;

  public SshConnectionService(MonitorProperties monitorProperties) {
    this.monitorProperties = monitorProperties;
  }

  public String execute(String host) throws JSchException, IOException, InterruptedException {
    Session session = openSession(host);
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

  private Session openSession(String host) throws JSchException {
    MonitorProperties.Ssh ssh = monitorProperties.getSsh();
    JSch jsch = new JSch();
    Session session = jsch.getSession(ssh.getUsername(), host, ssh.getPort());
    session.setPassword(ssh.getPassword());
    session.setConfig("StrictHostKeyChecking", "no");
    return session;
  }
}
