package com.wissensalt.springbootgooglepubsub.subscriber;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import com.wissensalt.springbootgooglepubsub.MyRuntimeException;
import com.wissensalt.springbootgooglepubsub.Payload;
import com.wissensalt.springbootgooglepubsub.property.ApplicationProperty;
import com.wissensalt.springbootgooglepubsub.property.PubSubProperty;
import com.wissensalt.springbootgooglepubsub.publisher.MyDlqPublisher;
import com.wissensalt.springbootgooglepubsub.publisher.MySourcePublisher;
import com.wissensalt.springbootgooglepubsub.publisher.AbstractPubSubPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MySourceSubscriber extends AbstractPubSubSubscriber {

  private final PubSubProperty pubSubProperty;
  private final ObjectMapper objectMapper;
  private final MySourcePublisher mySourcePublisher;
  private final MyDlqPublisher dlqPublisher;
  private final ApplicationProperty applicationProperty;

  public MySourceSubscriber(
      PubSubTemplate pubSubTemplate,
      PubSubProperty pubSubProperty,
      ObjectMapper objectMapper,
      MySourcePublisher mySourcePublisher,
      MyDlqPublisher dlqPublisher,
      ApplicationProperty applicationProperty
  ) {
    super(pubSubTemplate);
    this.pubSubProperty = pubSubProperty;
    this.objectMapper = objectMapper;
    this.mySourcePublisher = mySourcePublisher;
    this.dlqPublisher = dlqPublisher;
    this.applicationProperty = applicationProperty;
  }

  @Override
  public String getSubscription() {
    return pubSubProperty.getSource().getSubscriber();
  }

  @Override
  public boolean isDlqEnabled() {
    return pubSubProperty.getEnableDlq();
  }

  @Override
  public AbstractPubSubPublisher getDlqPublisher() {
    return dlqPublisher;
  }

  @Override
  public AbstractPubSubPublisher getSourcePublisher() {
    return mySourcePublisher;
  }

  @Override
  public int getMaxRetryNumber() {
    return pubSubProperty.getMaxRetryNumber();
  }

  @Override
  public int getRetryDelayDurationInMs() {
    return pubSubProperty.getRetryDelayInMilliseconds();
  }

  @Override
  public boolean isEnableRetry() {
    return pubSubProperty.getEnableRetrySubscriber();
  }

  @Override
  protected void receive(BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage) {
    final PubsubMessage message = basicAcknowledgeablePubsubMessage.getPubsubMessage();
    try {
      Payload payload = objectMapper.readValue(message.getData().toStringUtf8(), Payload.class);
      if (isTrue(applicationProperty.getSimulateDlq())) {
        int x = 1 / 0; // intentional error to simulate DLQ
      } else {
        log.info("Message received from subscriber {} with value {}", getSubscription(), payload);
        basicAcknowledgeablePubsubMessage.ack();
      }
    } catch (Exception e) {
      log.error("Error consuming message from subscriber {}", getSubscription(), e);
      if (isTrue(pubSubProperty.getEnableRetrySubscriber())) {
        throw new MyRuntimeException(e);
      } else {
        basicAcknowledgeablePubsubMessage.nack();
      }
    }
  }
}
