package com.github.marschall.sslsocketfactoryfactorybean;

import java.util.function.Supplier;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} that creates a {@link SSLSocketFactory}.
 */
public final class SSLSocketFactoryFactoryBean extends AbstractSSLSocketFactoryFactoryBean implements FactoryBean<SSLSocketFactory> {

  /**
   * Default constructor.
   */
  public SSLSocketFactoryFactoryBean() {
    super();
  }

  @Override
  public SSLSocketFactory getObject() {
    Supplier<SSLSocketFactory> sslSocketFactorySupplier = this.createSslSocketFactorySupplier();
    String[] cipherSuites = this.getCipherSuitesArray();
    String[] protocols = this.getProtocolsArray();
    return new DelegatingSSLSocketFactory(sslSocketFactorySupplier, cipherSuites, protocols);
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class;
  }


}
