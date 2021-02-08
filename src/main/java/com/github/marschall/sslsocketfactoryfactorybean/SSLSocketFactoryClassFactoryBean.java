package com.github.marschall.sslsocketfactoryfactorybean;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.FactoryBean;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.MethodArguments;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.TypeManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.UsingLookup;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;

public final class SSLSocketFactoryClassFactoryBean extends AbstractSSLSocketFactoryFactoryBean implements FactoryBean<Class<? extends SSLSocketFactory>> {

  @Override
  public Class<? extends SSLSocketFactory> getObject() {
    // TODO verify required properties
    // TODO cache instance?
    Supplier<SSLSocketFactory> sslSocketFactorySupplier = this.createSslSocketFactorySupplier();
    Class<?> dynamicType = new ByteBuddy()
            .subclass(DelegatingSSLSocketFactory.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
            .modifiers(Visibility.PUBLIC, TypeManifestation.FINAL)
            .defineField("defaultInstance", SocketFactory.class, Visibility.PRIVATE, Ownership.STATIC, FieldManifestation.FINAL)
//            .initializer(MethodCall.construct(null))
            .defineMethod("getDefault", SocketFactory.class, Visibility.PUBLIC, Ownership.STATIC, MethodArguments.PLAIN)
            .intercept(FieldAccessor.ofField("defaultInstance"))
            .make()
            .load(this.getClass().getClassLoader(), UsingLookup.of(MethodHandles.lookup()))
            .getLoaded();
    return dynamicType.asSubclass(SSLSocketFactory.class);
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class.getClass();
  }

}
