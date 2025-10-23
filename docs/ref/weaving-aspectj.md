# Aspect Weaving (AspectJ) — Private/Self Calls

Use AspectJ weaving to log private, protected and self-invoked methods that Spring AOP proxies cannot intercept. Logged supports both Spring AOP (default) and AspectJ weaving (opt-in at runtime or build time).

- Weaving bridge: `io.github.darkona.logged.weaving.WeavedAspect` delegates to the engine via `LoggedBridge`/`BridgeInstaller`.
- Auto-switch: the autoconfiguration detects weaving and switches from the Spring AOP aspect to the bridge.

Sources
- `src/main/java/io/github/darkona/logged/weaving/WeavedAspect.java:1`
- `src/main/java/io/github/darkona/logged/weaving/LoggedBridge.java:1`
- `src/main/java/io/github/darkona/logged/weaving/BridgeInstaller.java:1`
- `src/main/java/io/github/darkona/logged/weaving/Conditions.java:1`

---

## When To Use Weaving

- You need to log:
  - private/protected methods
  - calls inside the same class (self-invocation)
  - non-Spring managed components
- You accept LTW/CTW trade-offs (startup cost, tooling).

---

## How Detection Works

Logged switches to weaving mode when both are true:
- JVM launched with a weaving agent: `-javaagent:…aspectjweaver…` (or `spring-instrument`)
- An `aop.xml` exists on the classpath with `<aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>`

Then `BridgeInstaller` is created (instead of the Spring `LoggedAspect`) and installs the engine into the static `LoggedBridge` used by `WeavedAspect`.

---

## Load-Time Weaving (LTW)

1) Add dependency

Gradle (Groovy)
```groovy
dependencies {
  runtimeOnly("org.aspectj:aspectjweaver:1.9.21")
}

configurations { aspectjWeaver }
dependencies { aspectjWeaver "org.aspectj:aspectjweaver:1.9.21" }

tasks.named('bootRun') {
  jvmArgs += ["-javaagent:${configurations.aspectjWeaver.singleFile}"]
}
```

Maven
```xml
<dependency>
  <groupId>org.aspectj</groupId>
  <artifactId>aspectjweaver</artifactId>
  <version>1.9.21</version>
  <scope>runtime</scope>
  </dependency>
```

2) Create `META-INF/aop.xml`

`src/main/resources/META-INF/aop.xml`
```xml
<!DOCTYPE aspectj PUBLIC "-//AspectJ//DTD ASPECTJ 1.5.0//EN" "https://www.eclipse.org/aspectj/dtd/aspectj_1_5_0.dtd">
<aspectj>
  <aspects>
    <!-- Logged weaving entry point -->
    <aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>
  </aspects>
  <weaver options="-verbose -showWeaveInfo">
    <!-- Include your application packages -->
    <include within="com.example..*"/>
    <!-- And the Logged weaving package -->
    <include within="io.github.darkona.logged..*"/>
  </weaver>
  </aspectj>
```

3) Launch the JVM with the agent

```bash
java -javaagent:/path/to/aspectjweaver.jar -jar app.jar
```

Notes
- You may use `-javaagent:/path/to/spring-instrument.jar` as an alternative agent if your platform favors it.
- For tests, also add the agent to your test JVM or configure your build tool accordingly.

---

## Compile-Time Weaving (CTW)

Weave classes at build time. No `-javaagent` at runtime.

- Gradle: use an AspectJ plugin (e.g., FreeFair) and include this library on the AspectJ aspect path.
- Maven: `aspectj-maven-plugin` with `<aspectLibraries>` containing this library and your `aop.xml` on the classpath.

Gradle (Groovy) with FreeFair post-compile weaving
```groovy
plugins {
  id 'java'
  id 'io.freefair.aspectj.post-compile-weaving' version '8.6'
}

repositories { mavenCentral() }

dependencies {
  implementation "io.github.darkona:logged:1.4.0"        // use your version
  aspect        "io.github.darkona:logged:1.4.0"        // add to AspectJ aspect path
}

// Keep your aop.xml in resources so it’s on the classpath
// src/main/resources/META-INF/aop.xml
```

Minimal Maven example
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>aspectj-maven-plugin</artifactId>
  <version>1.15.0</version>
  <configuration>
    <complianceLevel>21</complianceLevel>
    <source>21</source>
    <target>21</target>
    <aspectLibraries>
      <aspectLibrary>
        <groupId>io.github.darkona</groupId>
        <artifactId>logged</artifactId>
      </aspectLibrary>
    </aspectLibraries>
    <weaveDependencies>true</weaveDependencies>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>compile</goal>
        <goal>test-compile</goal>
      </goals>
    </execution>
  </executions>
  </plugin>
```

Keep `META-INF/aop.xml` in your app resources so the aspect is enabled.

---

## Pointcut & Delegation

- `WeavedAspect` uses the same pointcut as the Spring aspect:
  - `execution(* *(..)) && (@annotation(io.github.darkona.logged.Logged) || @within(io.github.darkona.logged.Logged))`
- It delegates to `LoggedBridge.log(pjp)`, which calls the autoconfigured `LoggedEngine` installed by `BridgeInstaller`.

This keeps behavior identical across Spring AOP and AspectJ modes.

---

## Verifying Weaving

- Start with `-verbose` (in `aop.xml` weaver options) to see weave info at startup.
- Enable DEBUG for `io.github.darkona.logged.weaving` and check logs:
  - Spring bean `bridgeInstaller` present
  - No `springAspect` bean
- Functional check: annotate a private/self-invoked method and confirm it logs.

---

## Troubleshooting

- No weaving detected
  - Ensure `-javaagent:…aspectjweaver…` is present.
  - Ensure `META-INF/aop.xml` is packaged in the final JAR.
  - Ensure `<aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>` is present.
  - Include your application packages in `<weaver>` `include` entries.
- Conflicts with other agents
  - Only one LTW agent is typically required; remove duplicate/legacy agents.
- Performance
  - Weave only necessary packages to reduce startup and class-load overhead.

---

## Example: Self-Invocation

```java
@Service
public class BillingService {
  @Logged
  public void createInvoice() { saveLineItem(); }

  @Logged
  private void saveLineItem() { /* … */ }
}
```

- Spring AOP proxies don’t see `saveLineItem()` when called from the same instance — AspectJ weaving does.
