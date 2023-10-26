package com.wissensalt.springbootgooglepubsub.publisher;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
public abstract class PubSubPublisher {

  private final PubSubTemplate pubSubTemplate;

  protected PubSubPublisher(PubSubTemplate pubSubTemplate) {
    this.pubSubTemplate = pubSubTemplate;
  }

  public abstract String getTopic();

  protected abstract boolean enableRetryPublisher();

  public void publish(String message, Map<String, String> headers) {
    log.info("Publishing to topic [{}], message: [{}], headers: {}",
        getTopic(),
        message,
        headers
    );

    pubSubTemplate.publish(getTopic(), message, headers).addCallback(
        new ListenableFutureCallback<>() {
          @Override
          public void onFailure(Throwable ex) {
            if (enableRetryPublisher()) {
              final Timer timer = new Timer();
              timer.schedule(new TimerTask() {
                @Override
                public void run() {
                  publish(message, headers);
                }
              }, 500);
            }
          }

          @Override
          public void onSuccess(String messageId) {
            log.info("Success Publish Message with Message Id {}", messageId);
          }
        });
  }
}
