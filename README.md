# Logged

**Logged** is a lightweight Spring Boot library for method-level logging using Aspect-Oriented Programming (AOP). It provides an annotation-based mechanism to log method entry, return values, and exceptions using @Logged. The library now also supports MDC integration, OpenTelemetry span enrichment, and flexible decoration utilities.

## Features

- Annotate methods with `@Logged` to automatically log:
    
    - Method entry with argument names and values (names might need a compiler option)
        
    - Method return values
        
    - Exceptions thrown
        
    - Execution time
        
- Supports customization of log levels per operation (entry, return, exception)
    
- Class-level annotation support (logs all public methods of a class)
    
- Redaction of sensitive arguments by name or position
    
- Configurable message templates with token interpolation
    
- Global configuration via Spring Boot properties (`application.yml` / `application.properties`)
    
- MDC integration: injects method and trace context into the logging MDC
    
- OpenTelemetry integration: enriches active spans with method metadata
    
- Colorized and icon-decorated output (ANSI)
    
- Utility modules for banners, string interpolation, text transformation, and log decoration
    
- AspectJ weaving support for private and self-invoked method logging

## Technologies

* Java 21
* Spring Boot
* Spring AOP
* AspectJ (via Spring Aspects)
* Logback (or any SLF4J-compatible backend)

## Installation

Add the library to your Spring Boot project:

Maven:

```xml
<dependency>
    <groupId>io.github.darkona</groupId>
    <artifactId>logged</artifactId>
    <version>1.2.0</version>
</dependency>
```

Gradle:

```groovy
dependencies {
   implementation "io.github.darkona:logged:1.2.0"
}
```

No manual configuration is needed. Spring Boot will detect and wire all components automatically.


# `@Logged` Annotation Reference

The core of this library is the `@Logged` annotation. You can place it on methods **or classes** (all public methods inside will be logged). It controls what is logged for method entry, arguments, return values, execution time, and exceptions.

---

## 🔧 Options

|Attribute|Default|Description|
|---|---|---|
|`onCall`|`true`|Log a message when the method is called.|
|`args`|`true`|Log argument names and types.|
|`argValues`|`ALL`|Controls whether argument values are logged. Values: `ALL`, `NONE`, `NULL`.|
|`onReturn`|`true`|Log when the method returns.|
|`returnValue`|`ALL`|Controls whether return values are logged (`ALL`, `NONE`, `NULL`).|
|`onException`|`true`|Log when the method throws an exception.|
|`time`|`true`|Log execution time in ms.|
|`callMsg`|`""`|Custom template for call messages (overrides global template).|
|`returnMsg`|`""`|Custom template for return messages.|
|`exceptionMsg`|`""`|Custom template for exception messages.|
|`level`|`INFO`|Log level for normal messages.|
|`exceptionLevel`|`ERROR`|Log level for exceptions.|
|`logStackTrace`|`false`|Whether to print the exception stack trace.|
|`redactArgValues`|`{}`|List of argument names to redact when logging.|
|`redactAtPos`|`{}`|List of argument positions (0-based) to redact.|


### Enum: `Values`

* `ALL`: log all values.
* `NONE`: do not log values.
* `NULL`: only log if value is `null`.

---

## 📚 Notes

* Place `@Logged` at class level to log **all methods**.
* Use `callMsg`, `returnMsg`, and `exceptionMsg` for method-specific custom messages.
* Combine with configuration properties (`application.yml`) to set global defaults.
* With AspectJ weaving enabled, `@Logged` also works on **private** and **self-invoked** calls.

---

## ⚙️ Configuration Properties

`Logged` can be customized through Spring Boot configuration (`application.yml` or `application.properties`).  
All properties are prefixed with `logged.`.

### 🔑 Core Settings

|Property|Default|Description|
|---|---|---|
|`logged.enabled`|`true`|Enable/disable the logging aspect globally.|
|`logged.color`|`true`|Apply ANSI colors to log messages.|
|`logged.icons`|`true`|Show icons in log messages (e.g. `↓○`, `↑○`, `↑x`).|
|`logged.useUtf8`|`true`|Use UTF-8 characters in console output (for icons & symbols).|

### 🎨 Icons

|Property|Default|Description|
|---|---|---|
|`logged.entryIcon`|`↓○`|Icon for method entry.|
|`logged.exitIcon`|`↑○`|Icon for method exit.|
|`logged.throwIcon`|`↑x`|Icon for exceptions.|

### 🧾 MDC Integration

|Property|Default|Description|
|---|---|---|
|`logged.mdc.enabled`|`true`|Enable MDC context injection.|
|`logged.mdc.classKey`|`class`|MDC key for class name.|
|`logged.mdc.methodKey`|`method`|MDC key for method name.|
|`logged.mdc.traceKey`|_(none)_|MDC key for trace ID if present.|
super

**Effect:** Puts method info (e.g., class, method, trace id if present) into MDC during the call, so your log pattern can include it, e.g.: `%X{class} %X{method}`.
### 📡 OpenTelemetry Integration

|Property|Default|Description|
|---|---|---|
|`logged.useOTel`|`true`|If enabled, injects `@Logged` method data into active OpenTelemetry spans.|
|`logged.otel.attributes`|_(none)_|Optional map of attribute keys to tokens for span enrichment.|

### 🔒 Redaction

|Property|Default|Description|
|---|---|---|
|`logged.redactMask`|`█`|Character used to mask sensitive values.|
|`logged.redactLength`|`5`|Length of the redacted replacement string.|

### 📝 Message Templates

Templates support **tokens** such as `{c}` (class), `{m}` (method), `{a}` (arguments), `{rV}` (return value), `{d}` (duration), etc.

|Property|Default|Example Output|
|---|---|---|
|`logged.callMsgNoArgs`|`{eI}{c}::{m} called.`|`↓○MyService::ping called.`|
|`logged.callMsgArgs`|`{eI}{c}::{m} called with args: [{a}]`|`↓○UserService::save called with args: [42, "foo"]`|
|`logged.exitMsg`|`{xI}{c}::{m} returned.`|`↑○MyService::ping returned.`|
|`logged.exitMsgValue`|`{xI}{c}::{m} returned with value: {rV}`|`↑○UserService::save returned with value: 123`|
|`logged.timeTakenMsg`|`Time taken: {d} ms`|`Time taken: 42 ms`|
|`logged.throwMsg`|`{tI}{c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})`|`↑xMyService::ping threw a NullPointerException: boom at MyService.run (MyService.java:42)`|
|`logged.argsTemplate`|`({c}) {k}={v}`|`(String) username=john`|

### Example (`application.yml`)

```yaml
logged:
  enabled: true
  color: true
  icons: true
  entryIcon: ">>"
  exitIcon: "<<"
  throwIcon: "!!"
  mdc:
    enabled: true
    classKey: "logged.class"
    methodKey: "logged.method"
  redactMask: "*"
  redactLength: 8
  useOTel: true
  callMsgArgs: "{eI}{c}.{m} args=[{a}]"
  exitMsgValue: "{xI}{c}.{m} → {rV} ({d}ms)"
```

---
## 🧩 Message Formatting with Tokens

You can customize how your log messages look for method entry, exit, and exception cases using configurable templates.

Each template supports a specific set of tokens. These tokens will be dynamically replaced at runtime depending on the log context.


### 📥 Entry Message Tokens (`format.entry`)

|Token|Description|
|---|---|
|`{m}`|Method name|
|`{t}`|Method type|
|`{c}`|Class simple name|
|`{C}`|Fully qualified class name|
|`{a}`|Arguments passed to the method|
|`{eI}`|Entry icon (from config)|

---

### 📤 Exit Message Tokens (`format.exit`)

The tokens from the call also work for the return message, and it adds:

|Token|Description|
|---|---|
|`{rV}`|Return value (null-safe)|
|`{rC}`|Return type class name|
|`{d}`|Duration of method execution in milliseconds|
|`{xI}`|Exit icon (from config)|

---

### 💥 Exception Message Tokens (`format.exception`)

|Token|Description|
|---|---|
|`{ex}`|Exception class name|
|`{eM}`|Exception message|
|`{ec}`|Class where the exception originated|
|`{em}`|Method where the exception originated|
|`{f}`|File name (from the stack trace, if present)|
|`{L}`|Line number (from the stack trace, if avail.)|
|`{tI}`|Throw icon (from config)|

---

ℹ️ **Notes**

- If a method has no arguments, `{a}` resolves to an empty string.
    
- If a method returns `void`, `{rV}` is not evaluated.
    
- Icons (`{eI}`, `{xI}`, `{tI}`) come from the configured icon properties.
    
- Tokens support **default values** using a colon syntax: `{token:default}`. If the token value is missing or empty, the default is used. For example, `{rV:N/A}` prints `N/A` if the return value is null. The default can also be left blank (`{m:}`) to suppress output when no value is present.

---

## 🖼️ Bannerizer

Generates ASCII or Unicode banners to decorate log output. Useful for making sections of logs visually stand out (e.g., at startup or important checkpoints).

**Tools provided:**

* Center text in a banner of fixed width.
* Fill surrounding space with symbols or patterns.
* Apply colors to banners for emphasis.

---

## 🎨 Colorizer

Applies ANSI color codes to strings. Provides helpers for foreground colors, highlights, and style resets. It underpins the color output seen in log messages when `logged.color=true`.

**Tools provided:**

* Predefined colors (green, red, yellow, blue, cyan, magenta, etc.).
* Bright/gray shades for secondary emphasis.
* Custom RGB values for fine-grained control.
* Reset sequences to clear applied styles.

---

## ✍️ StringInterpolator

A **generic** placeholder interpolation engine. It is **not tied** to Logged’s tokens: you pass any template string plus your own key→value map, and it resolves placeholders (e.g., `{user}`, `{id}`, `{m}`, etc.). Logged uses it for its tokens, but you can use it for arbitrary templates elsewhere in your app.

**Tools provided:**

* Interpolate `{key}` placeholders using a caller‑supplied map.
* Deterministic replacement order to avoid partial overlaps.
* Null‑safe handling (callers can decide how to represent `null`).
* Leave unknown placeholders untouched (when no entry is provided for a key).
* Basic escaping support for literal braces in templates.

---

## 🔧 Transformer

A small **string utilities** helper used throughout the library. It provides **static**, null‑safe helpers that other components (like the aspect and decorators) rely on when shaping text.

**Where it lives:** `io.github.darkona.logged.utils.Transformer`

**Design:** stateless, thread‑safe (`public static` methods), focused on defensive string handling.

**Tools provided:**

* **`objectString(Object)`** – Safe `toString()`. Returns `"null"` for nulls and a guarded fallback (`"toString Error: <Type>"`) if `toString()` throws.
* **`fill(String s, int amount)`** – Repeats `s` `amount` times. Handy for separators and padding.
* **`mask(String, Integer unmasked, Character mask)`** – Masks a string, preserving a leading prefix of length `unmasked`. Defaults to `'*'` when `mask` is null. Delegates to the `char[]` overload.
* **`mask(char[] bytes, Integer unmasked, Character mask)`** – Masking variant that avoids creating intermediate strings before masking (useful for sensitive data).
* **`daySuffix(int day)`** – English ordinal suffix for day numbers, with proper handling of `11–13` → `"th"`.
* **`getSubstring(String str, int begin, int end)`** – Bounds‑checked substring: returns `""` for null/empty; returns the original string if indexes are invalid.
* **`getSubstringUntil(String str, int begin, String delimiter)`** – Substring starting at `begin` and ending **before** the first occurrence of `delimiter`; returns a trimmed original when the delimiter isn’t found; `""` for null/empty input.
* **`capitalize(String s)`** – Capitalizes the first character; returns the input as‑is for null/empty.

> Note: The class also defines internal constants for ANSI reset and a UTF‑8 `OutputStreamWriter`, but these are not part of the public API and are used only as internal conveniences.

---

## 🎀 LogDecorator

An abstraction for applying visual decoration (colors, masking, banners) to log text. It provides the main API used by the aspect to format log output consistently.

**Tools provided:**

* Apply predefined colors (rainbow, green, red, etc.).
* Mask or redact sensitive values with custom characters.
* Build banners and ornaments for structured logs.
* Clear color codes for plain-text outputs.
* Capitalize or adjust case of text.
* Generate day suffixes (e.g., 1st, 2nd, 3rd).

# Enabling Private & Self-Invoked Method Logging

By default (Spring proxy AOP), `@Logged` only intercepts **public** methods and **won’t** trigger on **self-invocation** (a method in the same class calling another annotated method). To log **private/protected/package‑private** methods and **self calls**, use **AspectJ weaving**.

There are two supported approaches:

* **(Recommended) Load‑Time Weaving (LTW)** with the **AspectJ Java Agent**
* **Compile‑Time Weaving (CTW)** during build

---

## Load‑Time Weaving (LTW) with AspectJ Agent

### 1) Add dependencies

> The `spring-boot-starter-aop` brings Spring AOP support; `aspectjweaver` provides the runtime weaver used by the Java agent.
> These are already present since Logged includes them as transitive dependencies.

### 1) Enable load‑time weaving in Spring

### 2) Run the app with the AspectJ agent

You must attach the **AspectJ Java agent** at JVM startup so bytecode can be woven as classes load.

```bash
java -javaagent:./aspectjweaver.jar -jar app.jar
```

**Docker example**

```dockerfile
ADD https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/1.9.22.1/aspectjweaver-1.9.22.1.jar /opt/aspectjweaver.jar
ENTRYPOINT ["java","-javaagent:/opt/aspectjweaver.jar","-jar","/app/app.jar"]
```

If you are using Gradle you can do something like:
```groovy
//This task copies aspectJWeaver from your classpath to your build directory.
//This enables you to copy it together with your application jar into a container.
tasks.register("copyAgents", Copy) {
    from configurations.runtimeClasspath.filter {
        it.name.startsWith("aspectjweaver")
    }
    into project.layout.buildDirectory.dir("libs")
    rename { "aspectjweaver.jar" }
}
//When running the app use aspectjweaver as an agent.
bootRun {
    def aj = configurations.runtimeClasspath
            .filter { it.name.startsWith("aspectjweaver") }
            .singleFile
    println aj
    jvmArgs "-javaagent:${aj.absolutePath}"
}
```

### 3) Use selective weaving:

Add aop.xml to resources/META-INF with specific weaving like so:
```xml
<!DOCTYPE aspectj PUBLIC "-//AspectJ//DTD//EN" "https://www.eclipse.org/aspectj/dtd/aspectj.dtd">
<aspectj>

    <weaver>
        <!-- only weave classes in our application-specific packages and sub-packages -->
        <include within="com.your.package..*"/>
        <!-- add this second line if you have issues with classpath. Usually happens when running locally. -->
        <include within="io.github.darkona.logged..*"/>
    </weaver>

    <aspects>
        <!-- Weave the specialized Aspect -->
        <aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>
    </aspects>

</aspectj>
```
> **IMPORTANT** Logged is configured to only work with LTW if aop.xml exists and is specifically weaving the WeavedAspect class.

### 4) Results with LTW

* **Private/protected/package** methods are intercepted.
* **Self‑invocation** is intercepted (calls inside the same class get woven).
* No code changes to call via proxies or `AopContext` are required.

> If you previously relied on proxy AOP only, keep `spring-boot-starter-aop`; LTW augments it to weave actual bytecode at load time.

---

## Option B — Compile‑Time Weaving (CTW)

If you prefer to avoid a Java agent in production, you can weave classes during build.

**Maven (aspectj‑maven‑plugin)**

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>aspectj-maven-plugin</artifactId>
      <version>1.14.0</version>
      <configuration>
        <complianceLevel>21</complianceLevel>
        <source>21</source>
        <target>21</target>
        <showWeaveInfo>true</showWeaveInfo>
        <aspectLibraries>
          <!-- Your project that contains @Aspect classes -->
          <aspectLibrary>
            <groupId>io.github.darkona</groupId>
            <artifactId>logged</artifactId>
          </aspectLibrary>
        </aspectLibraries>
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
  </plugins>
</build>
```

**Gradle (example with AspectJ weaving)**

```gradle
plugins {
  id "java"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-aop")
  aspect("org.aspectj:aspectjrt:1.9.22.1")
  aspect("org.aspectj:aspectjweaver:1.9.22.1")
}

// Use a Gradle AspectJ plugin of your choice (e.g., io.freefair.aspectj)
plugins {
  id "io.freefair.aspectj.post-compile-weaving" version "8.10.2"
}
```

### Results with CTW

* Classes are already woven when packaged.
* No `-javaagent` flag needed at runtime.
* Behavior parity with LTW: **private** methods and **self calls** are logged.

---

## Notes & Tips

* **Proxy vs Weaving**: Spring proxy AOP is great for most cases but cannot see private methods or self‑invocation. AspectJ weaving modifies the bytecode to apply advices directly at join points.
* **Performance**: The overhead of weaving is at class‑load time (LTW) or build time (CTW). Runtime overhead is typically comparable or lower than proxy interception for dense call graphs.
* **Mixing**: It’s safe to keep Spring AOP enabled. LTW/CTW augments it so your `@Logged` aspect applies everywhere you expect.

---

## ✨ Examples

### Default usage

```java
@Logged
public String salute() {
    return "Hello World!";
}
```

**Output:**

```
INFO : ↓○ DemoService::salute called with args: []
INFO : ↑○ DemoService::salute returned with value: Hello World! Time taken: 4 ms
```

### Disable entry logging (`onCall = false`)

```java
@Logged(onCall = false)
public String hello() {
    return "Hi";
}
```

```
INFO : ↑○ DemoService::hello returned with value: Hi Time taken: 1 ms
```

### Disable argument details (`args = false`)

```java
@Logged(args = false)
public void doWork(int x, int y) { }
```

```
INFO : ↓○ DemoService::doWork called
```

### Log only null arguments (`argValues = NULL`)

```java
@Logged(argValues = Logged.Values.NULL)
public void process(String input) { }
```

```
INFO : ↓○ DemoService::process called with args: [(String) input=null]
```

### Disable return logging (`onReturn = false`)

```java
@Logged(onReturn = false)
public int compute() { return 42; }
```

```
INFO : ↓○ DemoService::compute called with args: []
```

### Hide return values (`returnValue = NONE`)

```java
@Logged(returnValue = Logged.Values.NONE)
public String hiddenReturn() { return "secret"; }
```

```
INFO : ↑○ DemoService::hiddenReturn returned. Time taken: 1 ms
```

### Custom messages (`callMsg`, `returnMsg`, `exceptionMsg`)

```java
@Logged(
  callMsg = "Starting calculation...",
  returnMsg = "Calculation finished with {rV}",
  exceptionMsg = "Calculation failed: {eM}"
)
public int calc(int a, int b) {
    return a / b;
}
```

```
INFO : Starting calculation...
INFO : Calculation finished with 5
```

### Change log levels (`level`, `exceptionLevel`)

```java
@Logged(level = Level.DEBUG, exceptionLevel = Level.WARN)
public void riskyOp() { throw new RuntimeException("oops"); }
```

```
DEBUG : ↓○ DemoService::riskyOp called with args: []
WARN  : ↑x DemoService::riskyOp threw a RuntimeException: oops
```

### Print stack traces (`logStackTrace = true`)

```java
@Logged(logStackTrace = true)
public void explode() { throw new RuntimeException("boom"); }
```

```
ERROR : ↑x DemoService::explode threw a RuntimeException: boom
    at com.example.DemoService.explode(DemoService.java:42)
```

### Redact arguments by name or position

```java
@Logged(redactArgValues = {"password"}, redactAtPos = {1})
public void login(String username, String password) { }
```

```
INFO : ↓○ DemoService::login called with args: [(String) username=john,(String) password=█████]
```

### Disable exception logging (`onException = false`)

```java
@Logged(onException = false)
public void quietFail() { throw new RuntimeException("fail"); }
```

```
INFO : ↓○ DemoService::quietFail called with args: []
```

## 🧪 Option‑by‑Option Examples

Below are focused examples showing how each option changes the output. Replace `DemoService` with your class.

> Log lines are abbreviated to the message part for brevity.

### `onCall`

```java
@Logged(onCall = false)
public String hello(String name) { return "Hi " + name; }
```

**Output:**

```
↑○ DemoService::hello returned with value: Hi Ana Time taken: 0 ms
```

### `args`

```java
@Logged(args = false)
public void save(int id, String note) { }
```

**Output:**

```
↓○ DemoService::save called.
↑○ DemoService::save returned.
```

### `argValues = NONE | NULL | ALL`

```java
@Logged(argValues = Logged.Values.NONE)
public void update(String nonSensitive, String secret) { }
```

```
↓○ DemoService::update called with args: [(String) nonSensitive=•, (String) secret=•]
```

```java
@Logged(argValues = Logged.Values.NULL)
public void maybeNull(String x) { }
```

```
↓○ DemoService::maybeNull called with args: [(String) x=null]
```

```java
@Logged(argValues = Logged.Values.ALL) // default
public void show(String x) { }
```

```
↓○ DemoService::show called with args: [(String) x="abc"]
```

### `onReturn`

```java
@Logged(onReturn = false)
public int sum(int a, int b) { return a + b; }
```

**Output:**

```
↓○ DemoService::sum called with args: [(int) a=2,(int) b=3]
```

### `returnValue = NONE | NULL | ALL`

```java
@Logged(returnValue = Logged.Values.NONE)
public String token() { return "XYZ"; }
```

```
↑○ DemoService::token returned.
```

```java
@Logged(returnValue = Logged.Values.NULL)
public String maybe() { return null; }
```

```
↑○ DemoService::maybe returned with value: null
```

```java
@Logged(returnValue = Logged.Values.ALL) // default
public String ping() { return "pong"; }
```

```
↑○ DemoService::ping returned with value: pong
```

### `onException`

```java
@Logged(onException = false)
public void fail() { throw new IllegalStateException("nope"); }
```

**Output:** *No exception log emitted by @Logged (your logger may still print uncaught exceptions).*

### `time`

```java
@Logged(time = false)
public void slow() throws InterruptedException { Thread.sleep(100); }
```

**Output:**

```
↓○ DemoService::slow called with args: []
↑○ DemoService::slow returned.
```

### `callMsg`, `returnMsg`, `exceptionMsg`

You can override global templates per method. All tokens from the Tokens Reference are valid.

```java
@Logged(callMsg = "{eI}{c}.{m} → args={a}",
        returnMsg = "{xI}{c}.{m} ⇒ {rV} ({d}ms)",
        exceptionMsg = "{tI}{c}.{m} !! {ex}: {eM} at {ec}.{em} ({f}:{L})")
public String greet(String name) { return "Hello, " + name; }
```

**Output:**

```
↓○ DemoService.greet → args=[(String) name="Ana"]
↑○ DemoService.greet ⇒ Hello, Ana (1ms)
```

### `level` (normal logs)

```java
@Logged(level = org.slf4j.event.Level.DEBUG)
public void verbose() { }
```

**Output level:** DEBUG (messages appear only when DEBUG is enabled).

### `exceptionLevel`

```java
@Logged(exceptionLevel = org.slf4j.event.Level.WARN)
public void warnOnly() { throw new RuntimeException("boom"); }
```

**Output level:** WARN for the exception line.

### `logStackTrace`

```java
@Logged(logStackTrace = true)
public void oops() { throw new RuntimeException("boom"); }
```

**Output (tail):**

```
…
	at com.example.DemoService.oops(DemoService.java:42)
```

### `redactArgValues` (by name)

```java
@Logged(redactArgValues = {"password", "token"})
public void login(String user, String password, String token) { }
```

**Output:**

```
↓○ DemoService::login called with args: [(String) user="ana",(String) password=█████,(String) token=█████]
```

### `redactAtPos` (by index)

```java
@Logged(redactAtPos = {1}) // redact second parameter only
public void pay(String cardHolder, String cardNumber) { }
```

**Output:**

```
↓○ DemoService::pay called with args: [(String) cardHolder="ana",(String) cardNumber=█████]
```

### Class‑level usage

Apply to all **public** methods in a class (private/self calls require AspectJ weaving):

```java
@Logged(level = org.slf4j.event.Level.DEBUG, returnValue = Logged.Values.NONE)
class DemoService {
  public String a() { return "ok"; }
  public void b() { }
}
```
## Quick Checklist

* [ ] Added `spring-boot-starter-aop` and `aspectjweaver`.
* [ ] Added `aop.xml` and it is weaving the `WeavedAspect` class specifically.
* [ ] Start JVM with `-javaagent:/path/to/aspectjweaver.jar` (for LTW).
* [ ] Verified that **private** and **self‑invoked** methods now produce `@Logged` entries.


## Architecture

* Uses Spring AOP (`@Aspect`) to intercept methods annotated with `@Logged`
* No runtime reflection required outside of Spring proxy context

## License
Logged is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).  
You may use it freely in commercial or open-source projects.  
If you modify the library itself and distribute those changes, you must publish them under the same license so the community benefits.

