package com.cosium.synapse_junit_extension;

import static java.util.Objects.requireNonNull;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.VolumeOptions;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ResourceReaper;

/**
 * @author Réda Housni Alaoui
 */
public class Synapse {

  private static final Logger LOGGER = LoggerFactory.getLogger(Synapse.class);

  private static final int HTTP_PORT = 8008;

  private static final String NETWORK_ALIAS = "matrix-server";
  private static final String ADMIN_USERNAME = "admin_user";
  private static final String ADMIN_PASSWORD = "admin_secret";

  private static final Map<String, String> VOLUME_LABELS =
      Map.of(DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL, DockerClientFactory.SESSION_ID);

  private final GenericContainer<?> container;

  private final String url;

  private Synapse(String dockerImageName, Network network) {
    String volumeName = "synapse-junit-extension.synapse." + UUID.randomUUID();

    try (GenericContainer<?> transientContainer =
        createContainer(dockerImageName, volumeName)
            .withCommand("generate")
            .waitingFor(
                new LogMessageWaitStrategy().withRegEx(".*A config file has been generated.*"))) {
      transientContainer.start();
    }

    container =
        createContainer(dockerImageName, volumeName)
            .withExposedPorts(HTTP_PORT)
            .withNetwork(network)
            .withNetworkAliases(NETWORK_ALIAS);
    container.start();

    HomeServerConfig serverConfig =
        container.copyFileFromContainer(
            "/data/homeserver.yaml",
            inputStream -> ObjectMappers.forYaml().readValue(inputStream, HomeServerConfig.class));

    url = String.format("http://%s:%s", "localhost", container.getMappedPort(HTTP_PORT));
    new SynapseClient(url)
        .createUser(serverConfig.registrationSharedSecret(), ADMIN_USERNAME, ADMIN_PASSWORD, true);
  }

  static CloseableResource<Synapse> start(String dockerImageName, Network network) {
    Synapse server = new Synapse(dockerImageName, network);
    return new CloseableResource<>() {

      @Override
      public Synapse resource() {
        return server;
      }

      @Override
      public void close() {
        server.stop();
      }
    };
  }

  private static GenericContainer<?> createContainer(String dockerImageName, String volumeName) {
    ResourceReaper.instance().registerLabelsFilterForCleanup(VOLUME_LABELS);

    return new GenericContainer<>(DockerImageName.parse(dockerImageName))
        .withLogConsumer(new Slf4jLogConsumer(LOGGER))
        .withStartupTimeout(Duration.ofMinutes(30))
        .withCreateContainerCmdModifier(
            command -> {
              HostConfig hostConfig = command.getHostConfig();
              requireNonNull(hostConfig);
              hostConfig.withMounts(
                  List.of(
                      new Mount()
                          .withVolumeOptions(new VolumeOptions().withLabels(VOLUME_LABELS))
                          .withSource(volumeName)
                          .withTarget("/data")));
            })
        .withEnv("SYNAPSE_SERVER_NAME", NETWORK_ALIAS)
        .withEnv("SYNAPSE_REPORT_STATS", "no");
  }

  public String url() {
    return url;
  }

  public String dockerUrl() {
    return "http://" + NETWORK_ALIAS + ":" + HTTP_PORT;
  }

  public String adminUsername() {
    return ADMIN_USERNAME;
  }

  public String adminPassword() {
    return ADMIN_PASSWORD;
  }

  private void stop() {
    container.stop();
  }
}
