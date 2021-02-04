package com.github.marschall.sslsocketfactoryfactorybean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SpringJUnitConfig
class SSLSocketFactoryFactoryBeanTest {

  private RestOperations restOperations;

  @Test
  void expired() {
    String expired = this.restOperations.getForObject("https://expired.badssl.com/", String.class);
    assertNotNull(expired);
  }

  @Test
  void selfSigned() {
    String selfSigned = this.restOperations.getForObject("https://self-signed.badssl.com/", String.class);
    assertNotNull(selfSigned);
  }

  @Test
  void untrustedRoot() {
    String untrustedRoot = this.restOperations.getForObject("https://untrusted-root.badssl.com/", String.class);
    assertNotNull(untrustedRoot);
  }

  @Configuration
  static class SSLConfiguration {

    @Bean
    RestOperations restTemplate(SSLSocketFactory sslSocketFactory) {
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

    @Bean
    FactoryBean<SSLSocketFactory> sslSocketFactory() {
      SSLSocketFactoryFactoryBean factoryBean = new SSLSocketFactoryFactoryBean();
      return factoryBean;
    }

  }

}
