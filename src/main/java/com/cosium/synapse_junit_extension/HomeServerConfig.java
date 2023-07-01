package com.cosium.synapse_junit_extension;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Réda Housni Alaoui
 */
class HomeServerConfig {
  private final String registrationSharedSecret;

  public HomeServerConfig(
      @JsonProperty("registration_shared_secret") String registrationSharedSecret) {
    this.registrationSharedSecret = registrationSharedSecret;
  }

  public String registrationSharedSecret() {
    return registrationSharedSecret;
  }
}
