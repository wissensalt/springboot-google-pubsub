package com.wissensalt.springbootgooglepubsub.subscriber;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.wissensalt.springbootgooglepubsub.publisher.AbstractPubSubPublisher;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;

@Slf4j
public abstract class AbstractPubSubSubscriber {

  protected final PubSubTemplate pubSubTemplate;

  protected AbstractPubSubSubscriber(PubSubTemplate pubSubTemplate) {
    this.pubSubTemplate = pubSubTemplate;
  }

  protected static final String DELIVERY_ATTEMPT = "delivery_attempt";

  public abstract String getSubscription();

  public abstract boolean isDlqEnabled();

  public abstract boolean isEnableRetry();

  public abstract int getMaxRetryNumber();

  public abstract AbstractPubSubPublisher getDlqPublisher();

  public abstract AbstractPubSubPublisher getSourcePublisher();

  public abstract int getRetryDelayDurationInMs();

  protected abstract void receive(BasicAcknowledgeablePubsubMessage message);

  @PostConstruct
  private void listen() {
    pubSubTemplate.subscribe(getSubscription(), message -> {
      try {
        receive(message);
      } catch (Exception ex) {
        log.error("An error occurred", ex);
        if (isEnableRetry()) {
          handleFailedConsumer(message);
        }
      }
    });
  }

  private void handleFailedConsumer(BasicAcknowledgeablePubsubMessage message) {
    final PubsubMessage originalMessage = message.getPubsubMessage();
    final ByteString originalPayload = originalMessage.getData();
    final String originalPayloadAsString = originalPayload.toStringUtf8();
    final int currentRetryNumber = getRetryNumber(originalMessage);
    log.debug("Entering error channel, retry {}", currentRetryNumber);
    if (isDlqEnabled() && currentRetryNumber > getMaxRetryNumber()) {
      log.error("Max number of retry reached, publishing to failed topic {}",
          getDlqPublisher().getTopic());
      getDlqPublisher().publish(originalPayloadAsString, Map.of());
    } else {
      final int newRetryNumber = currentRetryNumber + 1;
      log.warn("Retry {}, publishing to retry topic {}",
          newRetryNumber,
          getSourcePublisher().getTopic()
      );

      final Timer timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          final Map<String, String> headers = Map.of(
              DELIVERY_ATTEMPT,
              Integer.toString(newRetryNumber));
          getSourcePublisher().publish(
              originalPayloadAsString,
              headers
          );
        }
      }, getRetryDelayDurationInMs());
    }

    message.ack();
  }

  private int getRetryNumber(PubsubMessage message) {
    try {
      return Integer.parseInt(message.getAttributesOrDefault(DELIVERY_ATTEMPT, "0"));
    } catch (Exception e) {

      return 0;
    }
  }
}
