package com.wissensalt.springbootgooglepubsub.publisher;

import com.wissensalt.springbootgooglepubsub.property.PubSubProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Component;

@Component
public class MyDlqPublisher extends AbstractPubSubPublisher {

  private final PubSubProperty pubSubProperty;

  @Autowired
  protected MyDlqPublisher(
      PubSubTemplate pubSubTemplate,
      PubSubProperty pubSubProperty
  ) {
    super(pubSubTemplate);
    this.pubSubProperty = pubSubProperty;
  }

  @Override
  protected boolean enableRetryPublisher() {
    return true;
  }

  @Override
  public String getTopic() {
    return pubSubProperty.getDlq().getTopic();
  }
}
