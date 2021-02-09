package com.github.marschall.sslsocketfactoryfactorybean;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.BeanCreationException;
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

/**
 * A {@link FactoryBean} that creates a {@link SSLSocketFactory} class.
 *
 * <h2>Implementation Note</h2>
 * The class is generated in the same package as this class using {@link Lookup#defineClass(byte[])}.
 */
public final class SSLSocketFactoryClassFactoryBean extends AbstractSSLSocketFactoryFactoryBean implements FactoryBean<Class<? extends SSLSocketFactory>> {

  /**
   * Default constructor.
   */
  public SSLSocketFactoryClassFactoryBean() {
    super();
  }

  @Override
  public Class<? extends SSLSocketFactory> getObject() {
    Supplier<SSLSocketFactory> sslSocketFactorySupplier = this.createSslSocketFactorySupplier();
    String[] cipherSuites = this.getCipherSuitesArray();
    String[] protocols = this.getProtocolsArray();
    String defaultFieldName = "defaultInstance";
    Class<?> sockeFactoryClass = new ByteBuddy()
            .subclass(DelegatingSSLSocketFactory.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
            .modifiers(Visibility.PUBLIC, TypeManifestation.FINAL)
            .defineField(defaultFieldName, SocketFactory.class, Visibility.PRIVATE, Ownership.STATIC, FieldManifestation.VOLATILE)
            .defineMethod("getDefault", SocketFactory.class, Visibility.PUBLIC, Ownership.STATIC, MethodArguments.PLAIN)
              .intercept(FieldAccessor.ofField(defaultFieldName))
            .defineMethod("setDefault", Void.TYPE, Visibility.PACKAGE_PRIVATE, Ownership.STATIC, MethodArguments.PLAIN)
              .withParameter(SocketFactory.class)
            .intercept(FieldAccessor.ofField(defaultFieldName).setsArgumentAt(0))
            .make()
            .load(this.getClass().getClassLoader(), UsingLookup.of(MethodHandles.lookup()))
            .getLoaded();

    try {
      Constructor<?> socketFactoryContructor = sockeFactoryClass.getDeclaredConstructor(Supplier.class, String[].class, String[].class);
      Method setDefault = sockeFactoryClass.getDeclaredMethod("setDefault", SocketFactory.class);
      Object defaultInstance = socketFactoryContructor.newInstance(sslSocketFactorySupplier, cipherSuites, protocols);
      setDefault.invoke(null, defaultInstance);
    } catch (ReflectiveOperationException e) {
      throw new BeanCreationException("could not initialize default instance", e);
    }

    return sockeFactoryClass.asSubclass(SSLSocketFactory.class);
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class.getClass();
  }

}
