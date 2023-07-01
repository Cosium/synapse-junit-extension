package com.cosium.synapse_junit_extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
@EnableSynapse
class SynapseTest {

  @Test
  void test(Synapse synapse) throws IOException, InterruptedException {
    URI synapseUri = URI.create(synapse.url() + "/_matrix/static/");
    HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(synapseUri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    assertThat(response.body()).contains("Synapse is running");
    assertThat(response.statusCode()).isEqualTo(200);
  }
}
