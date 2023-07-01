package com.cosium.synapse_junit_extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import org.junit.jupiter.api.Test;

/**
 * @author Réda Housni Alaoui
 */
@EnableSynapse("matrixdotorg/synapse:v1.85.0")
class SynapseTest {

  @Test
  void test(Synapse synapse) throws IOException, InterruptedException {
    assertThat(SynapseExtension.DEFAULT_DOCKER_IMAGE_NAME).doesNotContain("1.85.0");

    URI versionsUri = URI.create(synapse.url() + "/_synapse/admin/v1/server_version");
    Versions versions =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(versionsUri).GET().build(),
                JsonHandlers.INSTANCE.handler(Versions.class))
            .body()
            .parse();
    assertThat(versions.serverVersion()).isEqualTo("1.85.0");
  }
}
