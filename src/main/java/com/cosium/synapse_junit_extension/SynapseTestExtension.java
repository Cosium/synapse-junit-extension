package com.cosium.synapse_junit_extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SynapseTestExtension implements BeforeAllCallback, ParameterResolver {

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
    private final String storeKey;

    public Environment(ExtensionContext context) {
      store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
      dockerImageName =
          context
              .getTestClass()
              .map(testClass -> testClass.getAnnotation(EnableSynapse.class))
              .map(EnableSynapse::value)
              .orElse(EnableSynapse.DEFAULT_DOCKER_IMAGE_NAME);
      storeKey = Synapse.class + "#" + dockerImageName;
    }

    public void addNewSynapseIfNeed() {
      if (store.get(storeKey) != null) {
        return;
      }
      CloseableResource<Synapse> server = Synapse.start(dockerImageName);
      store.put(storeKey + "#close", new JUnitCloseableResource(server::close));
      store.put(storeKey, server.resource());
    }

    public Synapse getSynapse() {
      return store.get(storeKey, Synapse.class);
    }
  }
}
