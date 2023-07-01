package com.cosium.synapse_junit_extension;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.util.Optional;

/**
 * @author Réda Housni Alaoui
 */
public class LogbackConfigurator extends BasicConfigurator {

  @Override
  public ExecutionStatus configure(LoggerContext lc) {
    super.configure(lc);
    Level rootLevel =
        Optional.ofNullable(System.getenv("SYNAPSE_JUNIT_EXTENSION_TEST_LOG_ROOT_LEVEL"))
            .map(Level::valueOf)
            .orElse(Level.OFF);
    lc.getLogger("ROOT").setLevel(rootLevel);
    return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
  }
}
