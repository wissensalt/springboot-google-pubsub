package com.wissensalt.springbootgooglepubsub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.core.subscriber.PubSubSubscriberTemplate;
import org.springframework.cloud.gcp.pubsub.support.DefaultPublisherFactory;
import org.springframework.cloud.gcp.pubsub.support.DefaultSubscriberFactory;
import org.springframework.cloud.gcp.pubsub.support.PublisherFactory;
import org.springframework.cloud.gcp.pubsub.support.SubscriberFactory;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PubSubConfig {

  @Bean
  public DefaultSubscriberFactory subscriberFactory(
      GcpProjectIdProvider projectIdProvider,
      CredentialsProvider credentialsProvider
  ) {
    final DefaultSubscriberFactory defaultSubscriberFactory =
        new DefaultSubscriberFactory(projectIdProvider);
    defaultSubscriberFactory.setCredentialsProvider(credentialsProvider);

    return defaultSubscriberFactory;
  }

  @Bean
  public DefaultPublisherFactory publisherFactory(
      GcpProjectIdProvider projectIdProvider,
      CredentialsProvider credentialsProvider
  ) {
    final DefaultPublisherFactory defaultPublisherFactory =
        new DefaultPublisherFactory(projectIdProvider);
    defaultPublisherFactory.setCredentialsProvider(credentialsProvider);

    return defaultPublisherFactory;
  }

  @Bean
  public PubSubSubscriberTemplate pubSubSubscriberTemplate(SubscriberFactory subscriberFactory) {

    return new PubSubSubscriberTemplate(subscriberFactory);
  }

  @Bean
  public PubSubTemplate pubSubTemplate(
      PublisherFactory publisherFactory,
      SubscriberFactory subscriberFactory,
      CredentialsProvider credentialsProvider
  ) {
    if (publisherFactory instanceof DefaultPublisherFactory && (credentialsProvider != null)) {
      ((DefaultPublisherFactory) publisherFactory).setCredentialsProvider(credentialsProvider);
    }

    return new PubSubTemplate(publisherFactory, subscriberFactory);
  }

  @Bean
  public PubSubMessageConverter pubSubMessageConverter() {
    return new JacksonPubSubMessageConverter(new ObjectMapper());
  }
}
