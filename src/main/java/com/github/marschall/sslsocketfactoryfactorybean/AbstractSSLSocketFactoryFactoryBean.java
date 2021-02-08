package com.github.marschall.sslsocketfactoryfactorybean;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

public abstract class AbstractSSLSocketFactoryFactoryBean implements ResourceLoaderAware {

  private boolean lazyInit;
  @Nullable
  private String truststoreLocation;
  @Nullable
  private String truststoreType;
  @Nullable
  private String truststorePassword;
  @Nullable
  private String keystroreLocation;
  @Nullable
  private String keystroreType;
  @Nullable
  private String keystrorePass;
  @Nullable
  private String protocol;
  @Nullable
  private List<String> cipherSuites;

  private ResourceLoader resourceLoader;

  AbstractSSLSocketFactoryFactoryBean() {
    // prevent subclassing from outside the package
    super();
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public boolean isLazyInit() {
    return this.lazyInit;
  }

  public void setLazyInit(boolean lazyInit) {
    this.lazyInit = lazyInit;
  }

  public String getTruststoreLocation() {
    return this.truststoreLocation;
  }

  public void setTruststoreLocation(String truststoreLocation) {
    this.truststoreLocation = truststoreLocation;
  }

  public String getTruststoreType() {
    return this.truststoreType;
  }

  public void setTruststoreType(String truststoreType) {
    this.truststoreType = truststoreType;
  }

  public String getTruststorePassword() {
    return this.truststorePassword;
  }

  public void setTruststorePassword(String truststorePassword) {
    this.truststorePassword = truststorePassword;
  }

  public String getKeystroreLocation() {
    return this.keystroreLocation;
  }

  public void setKeystroreLocation(String keystroreLocation) {
    this.keystroreLocation = keystroreLocation;
  }

  public String getKeystroreType() {
    return this.keystroreType;
  }

  public void setKeystroreType(String keystroreType) {
    this.keystroreType = keystroreType;
  }

  public String getKeystrorePass() {
    return this.keystrorePass;
  }

  public void setKeystrorePass(String keystrorePass) {
    this.keystrorePass = keystrorePass;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public List<String> getCipherSuites() {
    return this.cipherSuites;
  }

  public void setCipherSuites(List<String> cipherSuites) {
    this.cipherSuites = cipherSuites;
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
    return this.loadKeyStore(this.truststoreType, this.truststoreLocation, this.truststorePassword);
  }

  private KeyStore loadKeyStore() {
    return this.loadKeyStore(this.keystroreType, this.keystroreLocation, this.keystrorePass);
  }

  private KeyStore loadKeyStore(String type, String location, String pass) {
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
    Resource resource = this.resourceLoader.getResource(location);
    try (InputStream inputStream = resource.getInputStream()) {
      char[] password = pass == null ? null : pass.toCharArray();
      keyStore.load(inputStream, password);
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
    if (trustStore != null) {
      try {
        if (trustStore.size() == 0) {
          throw new BeanCreationException("Truststore is empty");
        }
      } catch (KeyStoreException e) {
        throw new BeanCreationException("Could not check truststore size", e);
      }
    }

    try {
      sslContext.init(this.loadKeyManagers(keyStore, this.keystrorePass), this.loadTrustManagers(trustStore), null);
    } catch (KeyManagementException e) {
      throw new RuntimeException("Could not initialize ssl context", e);
    }

    return sslContext;
  }

  Supplier<SSLSocketFactory> createSslSocketFactorySupplier() {
    if (this.lazyInit) {
      return new LazyValue<>(() -> this.createConfiguredSslContext().getSocketFactory());
    } else {
      SSLSocketFactory socketFactory = this.createConfiguredSslContext().getSocketFactory();
      return () -> socketFactory;
    }
  }

  String[] getCipherSuitesArray() {
    if (this.cipherSuites != null) {
      return this.cipherSuites.toArray(new String[0]);
    } else {
      return null;
    }
  }

}
