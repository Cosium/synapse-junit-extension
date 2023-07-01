package com.cosium.synapse_junit_extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Réda Housni Alaoui
 */
public class Versions {
  private final String serverVersion;

  @JsonCreator
  private Versions(@JsonProperty("server_version") String serverVersion) {
    this.serverVersion = serverVersion;
  }

  public String serverVersion() {
    return serverVersion;
  }
}
