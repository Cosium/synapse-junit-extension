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

  private static final String ADMIN_USERNAME = "admin_user";
  private static final String ADMIN_PASSWORD = "admin_secret";

  private static final Map<String, String> VOLUME_LABELS =
      Map.of(DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL, DockerClientFactory.SESSION_ID);

  private final GenericContainer<?> container;

  private final String hostname;
  private final int port;
  private final String url;
  private final String networkAlias;

  private Synapse(Starter starter) {
    String volumeName = "synapse-junit-extension.synapse." + UUID.randomUUID();

    networkAlias = starter.networkAlias;

    String dockerImageName = starter.dockerImageName;
    try (GenericContainer<?> transientContainer =
        createContainer(dockerImageName, volumeName, networkAlias)
            .withCommand("generate")
            .waitingFor(
                new LogMessageWaitStrategy().withRegEx(".*A config file has been generated.*"))) {
      transientContainer.start();
    }

    container =
        createContainer(dockerImageName, volumeName, networkAlias)
            .withExposedPorts(HTTP_PORT)
            .withNetwork(starter.network)
            .withNetworkAliases(networkAlias);
    container.start();

    HomeServerConfig serverConfig =
        container.copyFileFromContainer(
            "/data/homeserver.yaml",
            inputStream -> ObjectMappers.forYaml().readValue(inputStream, HomeServerConfig.class));

    hostname = "localhost";
    port = container.getMappedPort(HTTP_PORT);
    url = String.format("http://%s:%s", hostname, port);
    new SynapseClient(url)
        .createUser(serverConfig.registrationSharedSecret(), ADMIN_USERNAME, ADMIN_PASSWORD, true);
  }

  public static Starter starter() {
    return new Starter();
  }

  /**
   * @deprecated Use {@link #starter()} instead
   */
  @Deprecated(forRemoval = true)
  public static CloseableResource<Synapse> start(String dockerImageName, Network network) {
    return starter().dockerImageName(dockerImageName).network(network).start();
  }

  private static GenericContainer<?> createContainer(
      String dockerImageName, String volumeName, String networkAlias) {
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
        .withEnv("SYNAPSE_SERVER_NAME", networkAlias)
        .withEnv("SYNAPSE_REPORT_STATS", "no");
  }

  public boolean https() {
    return false;
  }

  public String hostname() {
    return hostname;
  }

  public int port() {
    return port;
  }

  public String url() {
    return url;
  }

  public boolean dockerHttps() {
    return false;
  }

  public String dockerHostname() {
    return networkAlias;
  }

  public int dockerPort() {
    return HTTP_PORT;
  }

  public String dockerUrl() {
    return "http://" + dockerHostname() + ":" + dockerPort();
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

  public static class Starter {
    private String dockerImageName = SynapseExtension.DEFAULT_DOCKER_IMAGE_NAME;
    private Network network;
    private String networkAlias = SynapseExtension.DEFAULT_NETWORK_ALIAS;

    private Starter() {}

    public Starter dockerImageName(String dockerImageName) {
      this.dockerImageName = requireNonNull(dockerImageName);
      return this;
    }

    public Starter network(Network network) {
      this.network = network;
      return this;
    }

    public Starter networkAlias(String networkAlias) {
      this.networkAlias = requireNonNull(networkAlias);
      return this;
    }

    public CloseableResource<Synapse> start() {
      Synapse server = new Synapse(this);
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
  }
}
