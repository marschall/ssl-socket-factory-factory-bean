package com.github.marschall.sslsocketfactoryfactorybean;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;

public final class SSLSocketFactoryClassFactoryBean implements FactoryBean<Class<SSLSocketFactory>> {

  @Override
  public Class<SSLSocketFactory> getObject() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class.getClass();
  }

}
