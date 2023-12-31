package com.cosium.synapse_junit_extension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * @author Réda Housni Alaoui
 */
class ObjectMappers {

  private static final ObjectMapper JSON_MAPPER =
      new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private static final ObjectMapper YAML_MAPPER =
      new ObjectMapper(new YAMLFactory())
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  public static ObjectMapper forJson() {
    return JSON_MAPPER;
  }

  public static ObjectMapper forYaml() {
    return YAML_MAPPER;
  }
}
