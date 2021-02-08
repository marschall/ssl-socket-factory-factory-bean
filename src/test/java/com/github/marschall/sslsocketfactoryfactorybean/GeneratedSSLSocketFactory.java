package com.github.marschall.sslsocketfactoryfactorybean;

import java.util.function.Supplier;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public final class GeneratedSSLSocketFactory extends DelegatingSSLSocketFactory {

  private static volatile SocketFactory defaultInstance;

  GeneratedSSLSocketFactory(Supplier<SSLSocketFactory> delegateSupplier,
          String[] cipherSuites, String[] protocols) {
    super(delegateSupplier, cipherSuites, protocols);
  }

  static void setDefault(SocketFactory defaultInstance) {
    GeneratedSSLSocketFactory.defaultInstance = defaultInstance;
  }

//  static void setDefault(Supplier<SSLSocketFactory> delegateSupplier, String[] cipherSuites, String[] protocols) {
//    GeneratedSSLSocketFactory.defaultInstance = new GeneratedSSLSocketFactory(delegateSupplier, cipherSuites, protocols);
//  }

  public static SocketFactory getDefault() {
    return defaultInstance;
  }

}
