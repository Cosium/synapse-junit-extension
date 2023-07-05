package com.cosium.synapse_junit_extension;

/**
 * @author Réda Housni Alaoui
 */
public interface CloseableResource<T> {

  T resource();

  void close();
}
