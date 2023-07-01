package com.cosium.synapse_junit_extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SynapseTestExtension implements BeforeAllCallback, ParameterResolver {

  @Override
  public void beforeAll(ExtensionContext context) {
    ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
    if (store.get(Synapse.class) != null) {
      return;
    }
    CloseableResource<Synapse> server = Synapse.start();
    store.put(Synapse.class + "#close", new JUnitCloseableResource(server::close));
    store.put(Synapse.class, server.resource());
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

    return extensionContext
        .getRoot()
        .getStore(ExtensionContext.Namespace.GLOBAL)
        .get(Synapse.class, Synapse.class);
  }
}
