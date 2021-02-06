package com.github.marschall.sslsocketfactoryfactorybean;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.MethodArguments;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.UsingLookup;
import net.bytebuddy.implementation.FixedValue;

public final class SSLSocketFactoryClassFactoryBean extends AbstractSSLSocketFactoryFactoryBean implements FactoryBean<Class<SSLSocketFactory>> {

  @Override
  public Class<SSLSocketFactory> getObject() {
    // TODO verify required properties
    // TODO cache instance?
    Supplier<SSLSocketFactory> sslSocketFactorySupplier = this.createSslSocketFactorySupplier();
    Class<?> dynamicType = new ByteBuddy()
            .subclass(DelegatingSSLSocketFactory.class) // TODO public, TODO constructor
            .defineMethod("getDefault", SocketFactory.class, Visibility.PUBLIC, Ownership.STATIC, MethodArguments.PLAIN)
            .intercept(FixedValue.value("Hello World!"))
            .make()
            .load(this.getClass().getClassLoader(), UsingLookup.of(MethodHandles.lookup()))
            .getLoaded();
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class.getClass();
  }

}
