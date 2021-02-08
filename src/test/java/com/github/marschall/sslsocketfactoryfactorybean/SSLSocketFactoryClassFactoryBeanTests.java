package com.github.marschall.sslsocketfactoryfactorybean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@TestPropertySource(properties = {
    "truststore.type=PKCS12",
    "truststore.location=file:target/generated-truststores/bad-ssl.p12",
    "truststore.password=changeit"
})
class SSLSocketFactoryClassFactoryBeanTests {

  @Autowired
  private Class<? extends SocketFactory> socketFactoryClass;

  @Test
  void getDefault() throws ReflectiveOperationException {
    assertNotNull(this.socketFactoryClass);

    int classModifiers = this.socketFactoryClass.getModifiers();
    assertTrue(Modifier.isPublic(classModifiers));
    assertTrue(Modifier.isFinal(classModifiers));

    // a subclass of DelegatingSSLSocketFactory
    assertEquals(DelegatingSSLSocketFactory.class, this.socketFactoryClass.getSuperclass(), "superclass");

    // copy constructor from superclass DelegatingSSLSocketFactory
    Constructor<?>[] constructors = this.socketFactoryClass.getDeclaredConstructors();
    assertEquals(1, constructors.length, "constructor count");
    Constructor<?> constructor = constructors[0];
    assertFalse(Modifier.isPublic(constructor.getModifiers()));

    // public static SocketFactory getDefault()
    Method[] methods = this.socketFactoryClass.getDeclaredMethods();
//    assertEquals(2, methods.length, "method count");
    Method getDefault = null;
    for (Method method : methods) {
      if (method.getName().equals("getDefault")) {
        int methodModifiers = method.getModifiers();
        assertTrue(Modifier.isPublic(methodModifiers));
        assertTrue(Modifier.isStatic(methodModifiers));
        assertEquals(0, method.getParameterCount());
        assertEquals(SocketFactory.class, method.getReturnType());
        getDefault = method;
      } else if (method.getName().equals("setDefault")) {
        int methodModifiers = method.getModifiers();
        assertFalse(Modifier.isPublic(methodModifiers));
        assertTrue(Modifier.isStatic(methodModifiers));
        assertEquals(1, method.getParameterCount());
        assertEquals(Void.TYPE, method.getReturnType());
      } else {
        // ignore
      }
    }

    assertNotNull(getDefault);
    Object defaultSocketFactory = getDefault.invoke(null);
    assertNotNull(defaultSocketFactory);
    assertSame(this.socketFactoryClass, defaultSocketFactory.getClass());

    // second invocation returns the same class
    assertSame(defaultSocketFactory, getDefault.invoke(null));
  }
  @Configuration
  static class SSLConfiguration {

    @Value("${truststore.type}")
    private String truststoreType;

    @Value("${truststore.location}")
    private String truststoreLocation;

    @Value("${truststore.password}")
    private String truststorePassword;

    @Bean
    FactoryBean<Class<? extends SSLSocketFactory>> sslSocketFactory() {
      SSLSocketFactoryClassFactoryBean factoryBean = new SSLSocketFactoryClassFactoryBean();
      factoryBean.setTruststoreType(this.truststoreType);
      factoryBean.setTruststoreLocation(this.truststoreLocation);
      factoryBean.setTruststorePassword(this.truststorePassword);
      factoryBean.setProtocol("TLSv1.2");
      // default supports TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
      factoryBean.setCipherSuites(Collections.singletonList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
      return factoryBean;
    }

  }

}
