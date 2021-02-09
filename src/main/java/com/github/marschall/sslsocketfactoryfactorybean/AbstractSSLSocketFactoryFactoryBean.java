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
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * Abstract base class for {@link FactoryBean}s that
 *
 * @see <a href="https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#resources">Spring Resource URL</a>
 */
public abstract class AbstractSSLSocketFactoryFactoryBean implements ResourceLoaderAware {

  private boolean lazyInit = false;
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
  private String keystrorePassword;
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

  /**
   * Gets the value of the lazy initialization flag.
   *
   * @return whether lazy initialization set
   */
  public boolean isLazyInit() {
    return this.lazyInit;
  }

  /**
   * Configures lazy initialization.
   * <p>
   * Lazy initialization delays the creation of the {@link SSLContext}
   * and the loading of the {@link KeyStore}s until the first
   * {@link SSLSocket} is created. This can help with application
   * context startup time as less IO is performed. However it also means
   * that discovery of configuration errors, eg. a wrong keystore
   * password, is also delayed until the {@link SSLSocket} is created.
   * <p>
   * Not to be confused with {@link org.springframework.beans.factory.SmartFactoryBean#isEagerInit()}
   * <p>
   * Default is {@code false}.
   *
   * @param lazyInit whether the {@link SSLContext} should be lazily
   *                 initialized
   */
  public void setLazyInit(boolean lazyInit) {
    this.lazyInit = lazyInit;
  }

  /**
   * Gets the location of the truststore as a Spring Resource URL.
   *
   * @return the location of the truststore
   */
  public String getTruststoreLocation() {
    return this.truststoreLocation;
  }
  /**
   * Sets the location of the truststore. The presence of this property
   * causes a truststore to be used.
   *
   * @param truststoreLocation the truststore location as a Spring
   *                           Resource URL,
   *                           loaded using the application contexts
   *                           {@link ResourceLoader}
   * @see KeyStore#load(InputStream, char[])
   */
  public void setTruststoreLocation(String truststoreLocation) {
    this.truststoreLocation = truststoreLocation;
  }

  /**
   * Gets the truststore type.
   *
   * @return the truststore type
   */
  public String getTruststoreType() {
    return this.truststoreType;
  }

  /**
   * Sets the truststore type.
   *
   * @param truststoreType the truststore type
   * @see KeyStore#getInstance(String)
   */
  public void setTruststoreType(String truststoreType) {
    this.truststoreType = truststoreType;
  }

  /**
   * Gets the truststore password.
   *
   * @return the truststore password
   */
  public String getTruststorePassword() {
    return this.truststorePassword;
  }

  /**
   * Sets the truststore password.
   *
   * @param truststorePassword the truststore password.
   * @see KeyStore#load(InputStream, char[])
   */
  public void setTruststorePassword(String truststorePassword) {
    this.truststorePassword = truststorePassword;
  }

  /**
   * Gets the location of the keystore as a Spring Resource URL.
   *
   * @return the location of the keystore
   */
  public String getKeystroreLocation() {
    return this.keystroreLocation;
  }

  /**
   * Sets the location of the keystore. The presence of this property
   * causes a keystore to be used.
   *
   * @param keystroreLocation the keystore location as a Spring
   *                          Resource URL,
   *                          loaded using the application contexts
   *                          {@link ResourceLoader}
   * @see KeyStore#load(InputStream, char[])
   */
  public void setKeystroreLocation(String keystroreLocation) {
    this.keystroreLocation = keystroreLocation;
  }

  /**
   * Gets the keystore type.
   *
   * @return the keystore type
   */
  public String getKeystroreType() {
    return this.keystroreType;
  }

  /**
   * Sets the keystore type.
   *
   * @param keystroreType the keystore type
   * @see KeyStore#getInstance(String)
   */
  public void setKeystroreType(String keystroreType) {
    this.keystroreType = keystroreType;
  }

  /**
   * Gets the keystore password.
   *
   * @return the keystore password
   */
  public String getKeystrorePassword() {
    return this.keystrorePassword;
  }

  /**
   * Sets the keystore password.
   *
   * @param keystrorePass the keystore password.
   * @see KeyStore#load(InputStream, char[])
   */
  public void setKeystrorePassword(String keystrorePass) {
    this.keystrorePassword = keystrorePass;
  }

  /**
   * Gets the TLS protocol.
   *
   * @return the TLS protocol
   */
  public String getProtocol() {
    return this.protocol;
  }

  /**
   * Sets the TLS protocol.
   *
   * @param protocol the TLS protocol name, eg {@code "TLSv1.2"}
   * @see SSLContext#getInstance(String)
   * @see SSLSocket#setEnabledProtocols(String[])
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Gets the cipher suites to use.
   *
   * @return the cipher suites to use
   */
  public List<String> getCipherSuites() {
    return this.cipherSuites;
  }

  /**
   * Sets the cipher suites to use.
   *
   * @param cipherSuites the cipher suites to use
   * @see SSLSocket#setEnabledCipherSuites(String[])
   */
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
    return this.loadKeyStore(this.keystroreType, this.keystroreLocation, this.keystrorePassword);
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
      sslContext.init(this.loadKeyManagers(keyStore, this.keystrorePassword), this.loadTrustManagers(trustStore), null);
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

  String[] getProtocolsArray() {
    if (this.protocol != null) {
      return new String[] {this.protocol};
    } else {
      return null;
    }
  }

}
