package com.cosium.synapse_junit_extension;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Allows a test class to make use of {@link Synapse} instance.
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
   * @return A key allowing to retrieve the {@link org.testcontainers.containers.Network} instance
   *     from {@link org.junit.jupiter.api.extension.ExtensionContext.Namespace#GLOBAL} that will be
   *     used by {@link Synapse}. If empty, {@link Synapse} will use default docker network.
   */
  String dockerNetworkStoreKey() default "";
}
