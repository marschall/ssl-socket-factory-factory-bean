SSLSocketFactory FactoryBean
============================

A Spring [FactoryBean](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/FactoryBean.html) for a [SSLSocketFactory](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/net/ssl/SSLSocketFactory.html) or `SSLSocketFactory` class.

Sometimes a framework or library does not support configuring SSL parameters like truststore, keystore, cipher suites or TLS versions directly but only by providing a `javax.net.ssl.SSLSocketFactory` instance or `javax.net.ssl.SSLSocketFactory` class. As `javax.net.ssl.SSLSocketFactory` is an abstract class a subclass has to be created that delegates to the implementation instance. This project aims to make this simpler by providing a Spring `FactoryBean` that takes care of this.


Debugging
---------

```
-Djavax.net.debug=ssl:handshake
```
