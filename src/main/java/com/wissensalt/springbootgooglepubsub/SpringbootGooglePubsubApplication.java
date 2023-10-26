package com.wissensalt.springbootgooglepubsub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.autoconfigure.pubsub.GcpPubSubAutoConfiguration;
import org.springframework.cloud.gcp.autoconfigure.pubsub.GcpPubSubReactiveAutoConfiguration;

/**
 * GcpPubSubAutoConfiguration excluded due to flexibility of authentication in each environment
 * for K8S environment, use ApplicationDefaultIdentity with is provided by Service Account.
 * Otherwise, for Local environment use local Json credential to impersonate Service Account
 */
@SpringBootApplication(exclude = {
    GcpPubSubAutoConfiguration.class,
    GcpPubSubReactiveAutoConfiguration.class
})
public class SpringbootGooglePubsubApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringbootGooglePubsubApplication.class, args);
  }

}
