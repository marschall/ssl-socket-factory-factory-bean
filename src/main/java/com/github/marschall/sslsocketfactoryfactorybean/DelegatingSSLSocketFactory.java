package com.github.marschall.sslsocketfactoryfactorybean;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

abstract class DelegatingSSLSocketFactory extends SSLSocketFactory {

  private final SSLSocketFactory delegate;

  DelegatingSSLSocketFactory(SSLContext sslContext) {
    this.delegate = sslContext.getSocketFactory();
  }

  @Override
  public String[] getDefaultCipherSuites() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getSupportedCipherSuites() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Socket createSocket() throws IOException {
    return this.delegate.createSocket();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return this.delegate.createSocket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
    return this.delegate.createSocket(host, port, localHost, localPort);
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    return this.delegate.createSocket(s, host, port, autoClose);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return this.delegate.createSocket(host, port);
  }

  @Override
  public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
    return this.delegate.createSocket(s, consumed, autoClose);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    return this.delegate.createSocket(address, port, localAddress, localPort);
  }

}
