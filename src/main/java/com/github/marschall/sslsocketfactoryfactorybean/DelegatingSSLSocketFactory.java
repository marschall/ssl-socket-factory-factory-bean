package com.github.marschall.sslsocketfactoryfactorybean;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.function.Supplier;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

class DelegatingSSLSocketFactory extends SSLSocketFactory {

  private final Supplier<SSLSocketFactory> delegateSupplier;
  private final String[] cipherSuites;

  DelegatingSSLSocketFactory(@NonNull Supplier<SSLSocketFactory> delegateSupplier, @Nullable String[] cipherSuites) {
    Objects.requireNonNull(delegateSupplier);
    this.delegateSupplier = delegateSupplier;
    this.cipherSuites = cipherSuites;
  }

  private SSLSocketFactory getDelegate() {
    return this.delegateSupplier.get();
  }

  @Override
  public String[] getDefaultCipherSuites() {
    if (this.cipherSuites != null) {
      return this.cipherSuites;
    } else {
      return this.getDelegate().getDefaultCipherSuites();
    }
  }

  @Override
  public String[] getSupportedCipherSuites() {
    if (this.cipherSuites != null) {
      return this.cipherSuites;
    } else {
      return this.getDelegate().getSupportedCipherSuites();
    }
  }

  @Override
  public Socket createSocket() throws IOException {
    return this.getDelegate().createSocket();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return this.getDelegate().createSocket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
    return this.getDelegate().createSocket(host, port, localHost, localPort);
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    return this.getDelegate().createSocket(s, host, port, autoClose);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return this.getDelegate().createSocket(host, port);
  }

  @Override
  public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
    return this.getDelegate().createSocket(s, consumed, autoClose);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    return this.getDelegate().createSocket(address, port, localAddress, localPort);
  }

}
