package com.wissensalt.springbootgooglepubsub.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationProperty {

  private Boolean isK8S;
  private Boolean simulateDlq;
}
