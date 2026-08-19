- `WeavedAspect` uses the same pointcut as the Spring aspect:
  - `execution(* *(..)) && !execution(* lambda(..)) && (@annotation(io.github.darkona.logged.Logged) || @within(io.github.darkona.logged.Logged))`
  - `execution` matters: an `@annotation` pointcut on its own also matches `call` join points, which adds one interception per call site.# Aspect Weaving (AspectJ) — Private/Self Calls

Use AspectJ weaving to log private, protected and self-invoked methods that Spring AOP proxies cannot intercept. Logged supports both Spring AOP (default) and AspectJ weaving (opt-in at runtime or build time).

- Weaving bridge: `io.github.darkona.logged.weaving.WeavedAspect` delegates to the engine via `LoggedBridge`/`BridgeInstaller`.
- Mode switch: `logged.weaving` selects the Spring AOP aspect or the woven one, never both. The default detects a load-time weaving agent.

Sources
- `src/main/java/io/github/darkona/logged/weaving/WeavedAspect.java:1`
- `src/main/java/io/github/darkona/logged/weaving/LoggedBridge.java:1`
- `src/main/java/io/github/darkona/logged/weaving/BridgeInstaller.java:1`
- `src/main/java/io/github/darkona/logged/weaving/Conditions.java:1`
- `src/main/java/io/github/darkona/logged/weaving/WeavingMode.java:1`
- `src/main/resources/META-INF/logged/logged-weaving.xml:1`

---

## When To Use Weaving

- You need to log:
  - private/protected methods
  - calls inside the same class (self-invocation)
  - non-Spring managed components
- You accept LTW/CTW trade-offs (startup cost, tooling).

---

## How Detection Works

The `logged.weaving` property picks the mode:

| Value | Behaviour |
|---|---|
| `auto` (default) | Detect load-time weaving: a weaving agent on the JVM command line (`-javaagent:…aspectjweaver…` or `spring-instrument`) **and** an `aop.xml` on the classpath naming `io.github.darkona.logged.weaving.WeavedAspect`. |
| `enabled` | Always use the woven aspect. Required for CTW, which leaves no agent for `auto` to find. |
| `disabled` | Always use Spring AOP, whatever the JVM arguments say. Escape hatch for a JVM that carries a weaving agent for some other library. |

In weaving mode `BridgeInstaller` is created instead of the Spring `LoggedAspect`, and it
installs the engine into the static `LoggedBridge` that `WeavedAspect` calls. The two are
mutually exclusive on purpose: register both and every annotated method is instrumented
twice.

Until the bridge holds an engine, `LoggedBridge.log` just calls `proceed()`, so calls that
happen before the Spring context is up run uninstrumented instead of failing.


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

Weave classes at build time. No `-javaagent` at runtime, no weaver startup cost.

CTW has three requirements that LTW does not, because ajc knows nothing about Spring
conditions and there is no agent to fill in the gaps at class load:

1. **logged on the aspect path, non-transitively.** The aspect path is what ajc weaves
   *from*. Left transitive, everything logged depends on gets scanned for aspects too.
2. **The allow-list, passed with `-xmlConfigured`.** The jar ships two aspects:
   `weaving.WeavedAspect`, which delegates through `LoggedBridge`, and
   `internals.LoggedAspect`, which takes the engine through its constructor. ajc weaves
   every `@Aspect` it reaches, so without the allow-list it also weaves `LoggedAspect` and
   the first annotated call dies with
   `NoAspectBoundException: LoggedAspect: method 'void <init>()' not found`.
   The file ships inside the jar at `META-INF/logged/logged-weaving.xml`.
3. **`logged.weaving: enabled`.** The default `auto` looks for a weaving agent, which CTW
   never has. Left on `auto`, Spring registers its proxy next to the woven aspect and every
   annotated method is instrumented twice.

### Gradle (Groovy) with FreeFair post-compile weaving

```groovy
plugins {
  id 'java'
  id 'io.freefair.aspectj.post-compile-weaving' version '9.5.0'
}

repositories { mavenCentral() }

configurations { loggedArtifact }

dependencies {
  implementation "io.github.darkona:logged:1.7.0"                    // use your version

  // What ajc weaves from.
  aspect("io.github.darkona:logged:1.7.0") { transitive = false }

  // The same jar again, as a plain file, to pull the allow-list out of it.
  loggedArtifact("io.github.darkona:logged:1.7.0") { transitive = false }
}

def weavingConfigDir = layout.buildDirectory.dir('aspectj')
def weavingConfig = weavingConfigDir.map { it.file('META-INF/logged/logged-weaving.xml') }

tasks.register('extractLoggedWeavingConfig', Copy) {
  from({ zipTree(configurations.loggedArtifact.singleFile) }) {
    include 'META-INF/logged/logged-weaving.xml'
  }
  into weavingConfigDir
}

tasks.withType(JavaCompile).configureEach {
  def ajc = extensions.findByName('ajc')   // the plugin hangs its AjcAction off the task
  if (ajc != null) {
    dependsOn 'extractLoggedWeavingConfig'
    ajc.options.compilerArgs.addAll('-xmlConfigured', weavingConfig.get().asFile.absolutePath)
    inputs.file(weavingConfig).withPropertyName('loggedWeavingConfig')
  }
}
```

```yaml
logged:
  weaving: enabled
```

Copying the five lines into your own project instead of extracting them from the jar works
just as well:

```xml
<aspectj>
  <aspects>
    <aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>
  </aspects>
</aspectj>
```

### Task inputs

Neither `ajc.options.compilerArgs` nor the file they point at are inputs of the compile task.
Without the explicit `inputs.file(...)` above, editing the allow-list leaves `compileJava`
`UP-TO-DATE` and the previous weaving stays in the class files. The symptom is a fix that
does nothing until the next `clean`.

### Multi-module builds

CTW weaves only the module being compiled. Apply the plugin in every module whose classes
should be woven, or weaving is intermittent across the app.

### Minimal Maven example

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
    <Xset>
      <!-- Point ajc at the allow-list, extracted or written by hand. -->
    </Xset>
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

---

## Pointcut & Delegation

- `WeavedAspect` uses the same pointcut as the Spring aspect:
  - `execution(* *(..)) && !execution(* lambda$*(..)) && (@annotation(io.github.darkona.logged.Logged) || @within(io.github.darkona.logged.Logged))`
  - The `execution` part matters. An `@annotation` pointcut on its own also matches `call`
    join points, which adds one interception per call site once the code is woven.
- It delegates to `LoggedBridge.log(pjp)`, which calls the autoconfigured `LoggedEngine` installed by `BridgeInstaller`.

This keeps behavior identical across Spring AOP and AspectJ modes.

---

## Verifying Weaving

Verify the count, not just that the app starts. A working setup logs one call/return pair
per annotated invocation; a broken one logs two, or none.

- Check the beans: `bridgeInstaller` present, `springAspect` absent. Both present means
  double instrumentation.
- LTW: add `-verbose` to the `aop.xml` weaver options to see weave info at startup.
- Functional check: annotate a private, a static, a final and a self-invoked method, call
  each one once, and count the log lines.

The repository runs exactly this check on every build. See `src/testWeaving` and the
`testWeaving` Gradle task.

---

## Troubleshooting

**`NoSuchMethodError: WeavedAspect.aspectOf()`**
The logged jar on your aspect path was compiled without ajc. Releases from 1.7.1 on are
compiled with it. Under LTW this never appeared, because the agent generates the factory
on class load.

**`NoAspectBoundException: LoggedAspect: method 'void <init>()' not found`**
ajc wove the Spring AOP aspect as well. Pass the allow-list with `-xmlConfigured`, see the
CTW section.

**Everything is logged twice**
Both instrumentation paths are active. Under CTW set `logged.weaving: enabled`, so Spring
stops registering its proxy. Under LTW check that no second aop.xml on the classpath names
the aspect a second time.

**Nothing is logged under CTW**
- `logged.weaving` still on `auto`: CTW has no agent for it to detect.
- The module was not woven: CTW weaves only the module being compiled.
- The compile task was `UP-TO-DATE` with the previous weaving, because the allow-list is not
  a declared task input. See the CTW section, or run `clean`.

**No weaving detected under LTW**
- Ensure `-javaagent:…aspectjweaver…` is present.
- Ensure `META-INF/aop.xml` is packaged in the final JAR.
- Ensure `<aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>` is present.
- Include your application packages in `<weaver>` `include` entries.

**Conflicts with other agents**
Only one LTW agent is typically required; remove duplicate or legacy agents. If another
library requires an agent and you do not want logged to switch modes, set
`logged.weaving: disabled`.

**Performance**
Weave only the necessary packages to reduce startup and class-load overhead. Under CTW the
cost moves to build time instead.
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
