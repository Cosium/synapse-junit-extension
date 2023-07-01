package com.cosium.synapse_junit_extension;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.Network;

/**
 * Allows a test class to make use of {@link Synapse}.
 *
 * @author Réda Housni Alaoui
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@ExtendWith(SynapseExtension.class)
public @interface EnableSynapse {

  /**
   * @return The name of the docker image to use. It must be compatible with {@link
   *     SynapseExtension#DEFAULT_DOCKER_IMAGE_NAME}
   */
  String value() default SynapseExtension.DEFAULT_DOCKER_IMAGE_NAME;

  /**
   * @return A {@link DockerNetworkProvider} class allowing to retrieve a {@link
   *     org.testcontainers.containers.Network} given an {@link
   *     org.junit.jupiter.api.extension.ExtensionContext}. The class must expose an empty
   *     constructor to allow {@link SynapseExtension} to initialize it.
   */
  Class<? extends DockerNetworkProvider> dockerNetworkProvider() default
      DefaultDockerNetworkProvider.class;

  class DefaultDockerNetworkProvider implements DockerNetworkProvider {
    @Override
    public Optional<Network> get(ExtensionContext context) {
      return Optional.empty();
    }
  }
}
