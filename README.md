[![Build Status](https://github.com/Cosium/synapse-junit-extension/actions/workflows/ci.yml/badge.svg)](https://github.com/Cosium/synapse-junit-extension/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.cosium.synapse_junit_extension/synapse-junit-extension.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.cosium.synapse_junit_extension%22%20AND%20a%3A%22synapse-junit-extension%22)

# Matrix Communication Client

Unit extension allowing to run tests against a real [Synapse](https://github.com/matrix-org/synapse) instance.

# Usage

```java

@EnableSynapse
class MyTest {

	@Test
	void test(Synapse synapse) {
		// Do something with 'synapse'
		// For performance reason This 'synapse' instance is shared with any test belonging to any class annotated with @EnableSynapse
		// 
	}
}
```

For performance reasons, a single Synapse instance is shared with any test belonging to any class annotated
with `@EnableSynapse`. Therefore, you can run a test suite containing as many test classes as you want, Synapse will be started only once. It will shutdown at the end of the JUnit runtime.

# Dependency

```xml

<dependency>
  <groupId>com.cosium.synapse_junit_extension</groupId>
  <artifactId>synapse-junit-extension</artifactId>
  <verion>${matrix-communication-client.version}</verion>
  <scope>test</scope>
</dependency>
```

# Requirements

* JDK 11+
* Docker
