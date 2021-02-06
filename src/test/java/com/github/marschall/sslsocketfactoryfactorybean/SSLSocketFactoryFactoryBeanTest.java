package com.github.marschall.sslsocketfactoryfactorybean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;

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
    "truststore.location=target/generated-truststores/bad-ssl.p12",
    "truststore.password=changeit",
    "tls.protocol=TLSv1.2"
})
class SSLSocketFactoryFactoryBeanTest {

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

    @Value("${tls.protocol}")
    private String protocol;

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
      factoryBean.setTruststoreType(this.truststoreType);
      factoryBean.setTruststoreLocation(this.truststoreLocation);
      factoryBean.setTruststorePassword(this.truststorePassword);
      factoryBean.setProtocol(this.protocol);
      return factoryBean;
    }

  }

}
