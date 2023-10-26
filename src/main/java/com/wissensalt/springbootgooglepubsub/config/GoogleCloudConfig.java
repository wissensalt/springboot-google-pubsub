package com.wissensalt.springbootgooglepubsub.config;

import static java.lang.String.format;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.wissensalt.springbootgooglepubsub.MyRuntimeException;
import com.wissensalt.springbootgooglepubsub.property.ApplicationProperty;
import com.wissensalt.springbootgooglepubsub.property.GoogleCloudProperty;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.cloud.gcp.core.DefaultGcpProjectIdProvider;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class GoogleCloudConfig {

  private final ApplicationProperty applicationProperty;
  private final GoogleCloudProperty googleCloudProperty;

  @Bean
  public GcpProjectIdProvider gcpProjectIdProvider() {

    return new DefaultGcpProjectIdProvider() {
      @Override
      public String getProjectId() {
        return googleCloudProperty.getProjectId();
      }
    };
  }

  @Bean
  public Credentials credentials() {
    if (BooleanUtils.isFalse(applicationProperty.getIsK8S())) {
      log.debug(format("Loading Credential file %s for non K8S env",
          googleCloudProperty.getCredentialFile()));
      final String filePath = googleCloudProperty.getCredentialFile();
      try {
        final File file = ResourceUtils.getFile(filePath);

        return GoogleCredentials.fromStream(new FileInputStream(file));
      } catch (IOException e) {
        log.error(format("Failed to get File %s and build credential", filePath), e);

        throw new MyRuntimeException(e);
      }
    }

    return null;
  }

  @Bean
  public CredentialsProvider credentialsProvider(@Nullable Credentials credentials) {

    return () -> {
      if (credentials != null) {
        return credentials;
      }

      return GoogleCredentials.getApplicationDefault();
    };
  }
}
