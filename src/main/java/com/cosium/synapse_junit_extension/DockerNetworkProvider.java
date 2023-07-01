package com.cosium.synapse_junit_extension;

import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.Network;

/**
 * @author Réda Housni Alaoui
 */
@FunctionalInterface
public interface DockerNetworkProvider {

  Optional<Network> get(ExtensionContext context);
}
