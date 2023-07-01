package com.cosium.synapse_junit_extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

/**
 * @author Réda Housni Alaoui
 */
class SynapseClient {
  private final HttpClient httpClient;
  private final String url;

  public SynapseClient(String url) {
    httpClient = HttpClient.newBuilder().build();
    while (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    this.url = url;
  }

  public void createUser(
      String registrationSharedSecret, String username, String password, boolean admin) {
    URI registerEndpoint = URI.create(String.format("%s/_synapse/admin/v1/register", url));
    HttpRequest getNonce = HttpRequest.newBuilder().GET().uri(registerEndpoint).build();
    String nonce;
    try {
      nonce =
          httpClient
              .send(getNonce, JsonHandlers.INSTANCE.handler(NonceContainer.class))
              .body()
              .parse()
              .nonce();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    Mac mac =
        HmacUtils.getInitializedMac(
            HmacAlgorithms.HMAC_SHA_1, registrationSharedSecret.getBytes(StandardCharsets.UTF_8));
    mac.update(nonce.getBytes(StandardCharsets.UTF_8));
    mac.update((byte) 0);
    mac.update(username.getBytes(StandardCharsets.UTF_8));
    mac.update((byte) 0);
    mac.update(password.getBytes(StandardCharsets.UTF_8));
    mac.update((byte) 0);
    if (admin) {
      mac.update("admin".getBytes(StandardCharsets.UTF_8));
    } else {
      mac.update("notadmin".getBytes(StandardCharsets.UTF_8));
    }
    String maxDigest = Hex.encodeHexString(mac.doFinal());

    HttpRequest registerRequest =
        HttpRequest.newBuilder(registerEndpoint)
            .header("Content-Type", "application/json")
            .POST(
                JsonHandlers.INSTANCE.publisher(
                    new RegisterUserCommand(nonce, username, username, password, admin, maxDigest)))
            .build();

    try {
      httpClient.send(registerRequest, ResponseInfo.handler()).body().assertSuccess();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private static class NonceContainer {

    private final String nonce;

    @JsonCreator
    public NonceContainer(@JsonProperty("nonce") String nonce) {
      this.nonce = nonce;
    }

    public String nonce() {
      return nonce;
    }
  }

  private static class RegisterUserCommand {

    private final String nonce;
    private final String username;
    private final String displayName;
    private final String password;
    private final boolean admin;
    private final String mac;

    public RegisterUserCommand(
        String nonce,
        String username,
        String displayName,
        String password,
        boolean admin,
        String mac) {
      this.nonce = nonce;
      this.username = username;
      this.displayName = displayName;
      this.password = password;
      this.admin = admin;
      this.mac = mac;
    }

    @JsonProperty("nonce")
    public String nonce() {
      return nonce;
    }

    @JsonProperty("username")
    public String username() {
      return username;
    }

    @JsonProperty("displayname")
    public String displayName() {
      return displayName;
    }

    @JsonProperty("password")
    public String password() {
      return password;
    }

    @JsonProperty("admin")
    public boolean admin() {
      return admin;
    }

    @JsonProperty("mac")
    public String mac() {
      return mac;
    }
  }
}
