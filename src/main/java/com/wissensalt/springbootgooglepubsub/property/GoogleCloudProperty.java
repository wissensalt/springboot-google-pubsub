package com.wissensalt.springbootgooglepubsub.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("google")
@Data
public class GoogleCloudProperty {

  private String projectId;
  private String credentialFile;
}
