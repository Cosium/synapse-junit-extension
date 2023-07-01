package com.cosium.synapse_junit_extension;

import static java.util.Objects.requireNonNull;

import org.junit.jupiter.api.extension.ExtensionContext;

class JUnitCloseableResource implements ExtensionContext.Store.CloseableResource {

  private final Runnable close;

  public JUnitCloseableResource(Runnable close) {
    this.close = requireNonNull(close);
  }

  @Override
  public void close() {
    close.run();
  }
}
