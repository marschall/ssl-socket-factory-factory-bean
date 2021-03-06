package com.github.marschall.sslsocketfactoryfactorybean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SpringJUnitConfig
@TestPropertySource(properties = {
    "truststore.type=PKCS12",
    "truststore.location=file:target/generated-truststores/bad-ssl.p12",
    "truststore.password=changeit"
})
class SSLSocketFactoryFactoryBeanTests {

  @Autowired
  private RestOperations restOperations;

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

    @Value("${truststore.type}")
    private String truststoreType;

    @Value("${truststore.location}")
    private String truststoreLocation;

    @Value("${truststore.password}")
    private String truststorePassword;

    @Bean
    FactoryBean<SSLSocketFactory> sslSocketFactory() {
      SSLSocketFactoryFactoryBean factoryBean = new SSLSocketFactoryFactoryBean();
      factoryBean.setTruststoreType(this.truststoreType);
      factoryBean.setTruststoreLocation(this.truststoreLocation);
      factoryBean.setTruststorePassword(this.truststorePassword);
      factoryBean.setProtocol("TLSv1.2");
      // default supports TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
      factoryBean.setCipherSuites(Collections.singletonList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
      return factoryBean;
    }

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

  }

}
