package com.wissensalt.springbootgooglepubsub.controller;

import static java.util.stream.Collectors.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wissensalt.springbootgooglepubsub.Payload;
import com.wissensalt.springbootgooglepubsub.property.PubSubProperty;
import com.wissensalt.springbootgooglepubsub.publisher.MyDlqPublisher;
import com.wissensalt.springbootgooglepubsub.publisher.MySourcePublisher;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.AcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MyController {

  private final MySourcePublisher mySourcePublisher;
  private final MyDlqPublisher myDlqPublisher;
  private final ObjectMapper objectMapper;
  private final PubSubTemplate pubSubTemplate;
  private final PubSubProperty pubSubProperty;

  @PostMapping("/publish-source")
  public Boolean publishMessage(@RequestBody Payload payload) {
    String payloadAsString;
    try {
      payload.setId(UUID.randomUUID());
      payloadAsString = objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    mySourcePublisher.publish(payloadAsString, Map.of());

    return true;
  }

  @PostMapping("/publish-dlq")
  public Boolean publishDlqMessage(@RequestBody Payload payload) {
    String payloadAsString;
    try {
      payload.setId(UUID.randomUUID());
      payloadAsString = objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    myDlqPublisher.publish(payloadAsString, Map.of());

    return true;
  }

  @GetMapping("/source")
  public List<String> pullDataFromSourceTopic() {
    final List<AcknowledgeablePubsubMessage> messages =
        pubSubTemplate.pull(
            pubSubProperty.getSource().getSubscriber(),
            100,
            true
        );

    return messages.stream().map(Object::toString).collect(toList());
  }

  @GetMapping("/dlq")
  public List<String> pullDataFromDlqTopic() {
    final List<AcknowledgeablePubsubMessage> messages =
        pubSubTemplate.pull(
            pubSubProperty.getDlq().getSubscriber(),
            100,
            true
        );

    return messages.stream().map(Object::toString).collect(toList());
  }
}
