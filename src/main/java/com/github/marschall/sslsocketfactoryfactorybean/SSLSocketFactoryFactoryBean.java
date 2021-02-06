package com.github.marschall.sslsocketfactoryfactorybean;

import java.util.function.Supplier;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;

public final class SSLSocketFactoryFactoryBean extends AbstractSSLSocketFactoryFactoryBean implements FactoryBean<SSLSocketFactory> {

  @Override
  public SSLSocketFactory getObject() {
    Supplier<SSLSocketFactory> sslSocketFactorySupplier = this.createSslSocketFactorySupplier();
    String[] cipherSuites = this.getCipherSuitesArray();
    return new DelegatingSSLSocketFactory(sslSocketFactorySupplier, cipherSuites);
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class;
  }


}
