SSLSocketFactory FactoryBean [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/ssl-socket-factory-factory-bean/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/ssl-socket-factory-factory-bean) [![Build Status](https://travis-ci.org/marschall/ssl-socket-factory-factory-bean.svg?branch=master)](https://travis-ci.org/marschall/ssl-socket-factory-factory-bean)
============================

A Spring [FactoryBean](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/FactoryBean.html) for a [SSLSocketFactory](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/net/ssl/SSLSocketFactory.html) or `SSLSocketFactory` class.

Sometimes a framework or library does not support configuring SSL parameters like truststore, keystore, cipher suites or TLS versions directly but only by providing a `javax.net.ssl.SSLSocketFactory` instance or `javax.net.ssl.SSLSocketFactory` class. As `javax.net.ssl.SSLSocketFactory` is an abstract class a subclass has to be created that delegates to the implementation instance. This project aims to make this simpler by providing a Spring `FactoryBean` that takes care of this.

This project has an optional dependency on [Byte Buddy](https://bytebuddy.net/) which is needed when a `SSLSocketFactory` class rather than a `SSLSocketFactory` is desired, eg for `com.sun.jndi.ldap.LdapCtx#SOCKET_FACTORY`.

Usage
-----

Define a bean of type `SSLSocketFactoryFactoryBean` and a bean of type `SSLSocketFactory` will be available in the application context.

```java
@Configuration
public class SSLConfiguration {

  // there are various ways how configuration could happen, Spring properties is just one option
  @Value("${truststore.type}")
  private String truststoreType;

  @Value("${truststore.location}")
  private String truststoreLocation;

  @Value("${truststore.password}")
  private String truststorePassword;

  // define the SSLSocketFactoryFactoryBean
  @Bean
  FactoryBean<SSLSocketFactory> sslSocketFactory() {
    SSLSocketFactoryFactoryBean factoryBean = new SSLSocketFactoryFactoryBean();
    factoryBean.setTruststoreType(this.truststoreType);
    factoryBean.setTruststoreLocation(this.truststoreLocation);
    factoryBean.setTruststorePassword(this.truststorePassword);
    // these values could also be configurable
    factoryBean.setProtocol("TLSv1.2");
    factoryBean.setCipherSuites(Collections.singletonList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
    return factoryBean;
  }

  @Bean
  RestOperations restTemplate(SSLSocketFactory sslSocketFactory /* created by the bean defined in #sslSocketFactory  */) {
    ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
      @Override
      protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (connection instanceof HttpsURLConnection) {
          HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
          httpsConnection.setSSLSocketFactory(sslSocketFactory);
        }
        super.prepareConnection(connection, httpMethod);
      }
    };
    return new RestTemplate(requestFactory);
  }

}
```


Debugging
---------

To easily verify that all configuration options are passed as desired use


```
-Djavax.net.debug=ssl:handshake
```
