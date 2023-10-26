package com.wissensalt.springbootgooglepubsub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Payload implements Serializable {

  private static final long serialVersionUID = 9089982056797869967L;

  @Nullable
  private UUID id;

  private String code;
  private String name;
}
