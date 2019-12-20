/*
Copyright ©2019 MarkLogic Corporation.
*/

package com.marklogic.vpp.ui;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

  @Value("${mlHost:localhost}")
  private String mlHost;

  @Value("${mlUsername:admin}")
  private String mlUsername;

  @Value("${mlPassword:admin}")
  private String mlPassword;

  @Value("${mlStagingPort:8010}")
  private int mlStagingPort;

  @Value("${mlDhfRoot:/my/dhf}")
  private String mlDhfRoot;

  /**
   * @return the mlStagingPort
   */
  public int getMlStagingPort() {
    return mlStagingPort;
  }

  public String getMlHost() {
    return mlHost;
  }

  public String getMlDhfRoot() {
    return mlDhfRoot;
  }

  @Autowired
  Environment environment;


  @Bean
  public RestTemplate restTemplate() {
    int springPort = Integer.parseInt(environment.getProperty("server.port"));

    HttpHost host = new HttpHost(mlHost, springPort, "http");
    CloseableHttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider())
        .useSystemProperties().build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactoryDigestAuth(host,
        client);

    return new RestTemplate(requestFactory);
  }

  private CredentialsProvider provider() {
    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(mlUsername, mlPassword);
    provider.setCredentials(AuthScope.ANY, credentials);
    return provider;
  }
}
