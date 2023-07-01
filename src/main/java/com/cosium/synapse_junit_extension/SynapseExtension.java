package com.cosium.synapse_junit_extension;

import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.testcontainers.containers.Network;

public class SynapseExtension implements BeforeAllCallback, ParameterResolver {

  public static final String DEFAULT_DOCKER_IMAGE_NAME = "matrixdotorg/synapse:v1.86.0";

  @Override
  public void beforeAll(ExtensionContext context) {
    new Environment(context).addNewSynapseIfNeed();
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return Synapse.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    return new Environment(extensionContext).getSynapse();
  }

  private static class Environment {

    private final ExtensionContext.Store store;
    private final String dockerImageName;
    private final String dockerNetworkStoreKey;
    private final String storeKey;

    public Environment(ExtensionContext context) {
      store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
      Optional<EnableSynapse> enableSynapse =
          context
              .getTestClass()
              .flatMap(
                  testClass -> AnnotationSupport.findAnnotation(testClass, EnableSynapse.class));
      dockerImageName = enableSynapse.map(EnableSynapse::value).orElse(DEFAULT_DOCKER_IMAGE_NAME);
      dockerNetworkStoreKey = enableSynapse.map(EnableSynapse::dockerNetworkStoreKey).orElse(null);
      storeKey = Synapse.class + "#" + dockerImageName;
    }

    public void addNewSynapseIfNeed() {
      if (store.get(storeKey) != null) {
        return;
      }
      Network network = null;
      if (dockerNetworkStoreKey != null && !dockerNetworkStoreKey.isBlank()) {
        network = store.get(dockerNetworkStoreKey, Network.class);
      }
      CloseableResource<Synapse> server = Synapse.start(dockerImageName, network);
      store.put(storeKey + "#close", new JUnitCloseableResource(server::close));
      store.put(storeKey, server.resource());
    }

    public Synapse getSynapse() {
      return store.get(storeKey, Synapse.class);
    }
  }
}
