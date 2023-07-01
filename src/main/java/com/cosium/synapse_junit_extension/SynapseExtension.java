package com.cosium.synapse_junit_extension;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
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
    private final EnableSynapseMetadata metadata;

    public Environment(ExtensionContext context) {
      store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
      metadata = new EnableSynapseMetadata(context);
    }

    public void addNewSynapseIfNeed() {
      if (store.get(metadata) != null) {
        return;
      }
      CloseableResource<Synapse> server =
          Synapse.start(metadata.dockerImageName, metadata.networkSupplier.get().orElse(null));
      store.put(new Object(), new JUnitCloseableResource(server::close));
      store.put(metadata, server.resource());
    }

    public Synapse getSynapse() {
      return store.get(metadata, Synapse.class);
    }
  }

  private static class EnableSynapseMetadata {
    private final String dockerImageName;
    private final Class<? extends DockerNetworkProvider> dockerNetworkProviderClass;
    private final Supplier<Optional<Network>> networkSupplier;

    EnableSynapseMetadata(ExtensionContext context) {
      Optional<EnableSynapse> enableSynapse =
          context
              .getTestClass()
              .flatMap(
                  testClass -> AnnotationSupport.findAnnotation(testClass, EnableSynapse.class));
      dockerImageName = enableSynapse.map(EnableSynapse::value).orElse(DEFAULT_DOCKER_IMAGE_NAME);
      dockerNetworkProviderClass =
          enableSynapse.map(EnableSynapse::dockerNetworkProvider).orElse(null);
      networkSupplier =
          () ->
              Optional.ofNullable(dockerNetworkProviderClass)
                  .map(ReflectionSupport::newInstance)
                  .flatMap(provider -> provider.get(context));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      EnableSynapseMetadata that = (EnableSynapseMetadata) o;
      return Objects.equals(dockerImageName, that.dockerImageName)
          && Objects.equals(dockerNetworkProviderClass, that.dockerNetworkProviderClass);
    }

    @Override
    public int hashCode() {
      return Objects.hash(dockerImageName, dockerNetworkProviderClass);
    }
  }
}
