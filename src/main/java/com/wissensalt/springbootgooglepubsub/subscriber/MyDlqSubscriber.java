package com.wissensalt.springbootgooglepubsub.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import com.wissensalt.springbootgooglepubsub.MyRuntimeException;
import com.wissensalt.springbootgooglepubsub.Payload;
import com.wissensalt.springbootgooglepubsub.property.PubSubProperty;
import com.wissensalt.springbootgooglepubsub.publisher.AbstractPubSubPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyDlqSubscriber extends AbstractPubSubSubscriber {

  private final PubSubProperty pubSubProperty;
  private final ObjectMapper objectMapper;

  @Autowired
  protected MyDlqSubscriber(
      PubSubTemplate pubSubTemplate,
      PubSubProperty pubSubProperty,
      ObjectMapper objectMapper
  ) {
    super(pubSubTemplate);
    this.pubSubProperty = pubSubProperty;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getSubscription() {
    return pubSubProperty.getDlq().getSubscriber();
  }

  @Override
  public boolean isDlqEnabled() {
    return false;
  }

  @Override
  public int getMaxRetryNumber() {
    return 0;
  }

  @Override
  public AbstractPubSubPublisher getDlqPublisher() {
    return null;
  }

  @Override
  public AbstractPubSubPublisher getSourcePublisher() {
    return null;
  }

  @Override
  public int getRetryDelayDurationInMs() {
    return 0;
  }

  @Override
  public boolean isEnableRetry() {
    return false;
  }

  @Override
  protected void receive(BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage) {
    final PubsubMessage message = basicAcknowledgeablePubsubMessage.getPubsubMessage();
    try {
      Payload payload = objectMapper.readValue(message.getData().toStringUtf8(), Payload.class);
      log.info("Message received from subscriber {} with value {}", getSubscription(), payload);
      basicAcknowledgeablePubsubMessage.ack();
    } catch (Exception e) {
      log.error("Error consuming message from subscriber {}", getSubscription());
      basicAcknowledgeablePubsubMessage.nack();
      throw new MyRuntimeException(e);
    }
  }
}
