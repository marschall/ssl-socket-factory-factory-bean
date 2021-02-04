package com.github.marschall.sslsocketfactoryfactorybean;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.MethodArguments;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.UsingLookup;
import net.bytebuddy.implementation.FixedValue;

public final class SSLSocketFactoryFactoryBean implements FactoryBean<SSLSocketFactory> {

  private String truststoreLocation;
  private String truststoreType;
  private String truststorePass;
  private String keystroreLocation;
  private String keystroreType;
  private String keystrorePass;
  private String protocol;
  private List<String> cipherSuites;

  @Override
  public SSLSocketFactory getObject() {
    // TODO verify required properties
    // TODO cache instance?
    SSLContext sslContext = this.createConfiguredSslContext();
    Class<?> dynamicType = new ByteBuddy()
            .subclass(DelegatingSSLSocketFactory.class) // TODO public, TODO constructor
            .defineMethod("getDefault", SocketFactory.class, Visibility.PUBLIC, Ownership.STATIC, MethodArguments.PLAIN)
            .intercept(FixedValue.value("Hello World!"))
            .make()
            .load(this.getClass().getClassLoader(), UsingLookup.of(MethodHandles.lookup()))
            .getLoaded();
  }

  @Override
  public Class<?> getObjectType() {
    return SSLSocketFactory.class;
  }

  private SSLContext createUnconfiguredSslContext() {
    try {
      if (this.protocol == null) {
        return SSLContext.getDefault();

      } else {
        return SSLContext.getInstance(this.protocol);
      }
    } catch (NoSuchAlgorithmException e) {
      throw new BeanCreationException("Could not create unintialized SSLContext with protocol: " + this.protocol, e);
    }
  }

  private KeyStore loadTrustStore() {
    return loadKeyStore(this.truststoreType, this.truststoreLocation, this.truststorePass);
  }

  private KeyStore loadKeyStore() {
    return loadKeyStore(this.keystroreType, this.keystroreLocation, this.keystrorePass);
  }

  private static KeyStore loadKeyStore(String type, String location, String pass) {
    if (location == null) {
      return null;
    }
    String typeToUse = type == null ? KeyStore.getDefaultType() : type;
    KeyStore keyStore;
    try {
      keyStore = KeyStore.getInstance(typeToUse);
    } catch (KeyStoreException e) {
      throw new BeanCreationException("Could not create key store of type: " + typeToUse, e);
    }
    try (FileInputStream fileInputStream = new FileInputStream(location)) {
      char[] password = pass == null ? null : pass.toCharArray();
      keyStore.load(fileInputStream, password);
    } catch (GeneralSecurityException | IOException e) {
      throw new BeanCreationException("Could not load key store from: " + location, e);
    }
    return keyStore;
  }

  private TrustManager[] loadTrustManagers(KeyStore trustStore) {
    if (trustStore == null) {
      return null;
    }
    String defaultTrustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    TrustManagerFactory trustManagerFactory;
    try {
      trustManagerFactory = TrustManagerFactory.getInstance(defaultTrustManagerAlgorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new BeanCreationException("Default algorithm not supported: " + defaultTrustManagerAlgorithm, e);
    }

    try {
      trustManagerFactory.init(trustStore);
    } catch (KeyStoreException e) {
      throw new BeanCreationException("Could not initialize trust manager factory", e);
    }
    return trustManagerFactory.getTrustManagers();
  }

  private KeyManager[] loadKeyManagers(KeyStore keyStore, String password) {
    if (keyStore == null) {
      return null;
    }
    String defaultKeyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    KeyManagerFactory keyManagerFactory;
    try {
      keyManagerFactory = KeyManagerFactory.getInstance(defaultKeyManagerAlgorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new BeanCreationException("Default algorithm not supported: " + defaultKeyManagerAlgorithm, e);
    }

    try {
      keyManagerFactory.init(keyStore, password == null ? null : password.toCharArray());
    } catch (GeneralSecurityException e) {
      throw new BeanCreationException("Could not initialize key manager factory", e);
    }
    return keyManagerFactory.getKeyManagers();
  }

  private SSLContext createConfiguredSslContext() {
    SSLContext sslContext = this.createUnconfiguredSslContext();

    KeyStore keyStore = this.loadKeyStore();
    KeyStore trustStore = this.loadTrustStore();

    try {
      sslContext.init(this.loadKeyManagers(keyStore, this.keystrorePass), this.loadTrustManagers(trustStore), null);
    } catch (KeyManagementException e) {
      throw new RuntimeException("Could not initialize ssl context", e);
    }
    return sslContext;
  }

}
