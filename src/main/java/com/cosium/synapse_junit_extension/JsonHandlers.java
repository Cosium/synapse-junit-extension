package com.cosium.synapse_junit_extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author Réda Housni Alaoui
 */
class JsonHandlers {

  public static final JsonHandlers INSTANCE = new JsonHandlers();

  private final ObjectMapper objectMapper;

  private JsonHandlers() {
    this.objectMapper = ObjectMappers.forJson();
  }

  public HttpRequest.BodyPublisher publisher(Object value) {
    try {
      String json = objectMapper.writeValueAsString(value);
      return HttpRequest.BodyPublishers.ofString(json);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  public <T> HttpResponse.BodyHandler<JsonBody<T>> handler(Class<T> type) {
    return responseInfo -> createBodySubscriber(responseInfo, type);
  }

  private <T> HttpResponse.BodySubscriber<JsonBody<T>> createBodySubscriber(
      HttpResponse.ResponseInfo responseInfo, Class<T> type) {
    int statusCode = responseInfo.statusCode();
    if (statusCode != 200) {
      return HttpResponse.BodySubscribers.replacing(new JsonBody<>(statusCode, null));
    }

    return HttpResponse.BodySubscribers.mapping(
        HttpResponse.BodySubscribers.ofInputStream(),
        inputStream -> {
          try {
            return new JsonBody<>(statusCode, objectMapper.readValue(inputStream, type));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
