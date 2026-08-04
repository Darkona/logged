# Logged

Logged is a lightweight Spring Boot library for method-level logging using Aspect-Oriented Programming (AOP). It provides an annotation-based mechanism to log method entry, return values, exceptions, and timing, with rich customization, masking, plugins, and optional weaving for private/self calls.

Think of it as a careful scribe for your code: precise, flexible, and useful.

Compatible with **Java 21**, **Spring Boot 3.x**, and **SLF4J backends (Logback/Log4j2)**.

---

## Features / Highlights

* ↓○ Entry, ↑○ Exit, ↑x Exception logging per method or per class (`@Logged`)
* Argument names/types/values with configurable masking and truncation
* Return value logging modes: `ALL`, `NONE`, `NULL`
* Execution time capture and thresholds: auto-promote slow calls and add markers
* Custom templates and icons; optional color decoration and icon themes
* Depth indicators for nested/self calls
* Global configuration via `application.yml`/`application.properties`
* Plugins:

    * **SLF4J formatting** with templates, icons, colors
    * **MDC enrichment**: injects class/method/trace context into MDC
    * **OpenTelemetry**: enriches spans with method metadata and MDC
    * **Logback/Log4j2 helpers**: marker filters and configuration glue
* AspectJ weaving option to log private and self-invoked methods
* Utilities for banners, colorized output, string interpolation, and text transformation

---

## Installation

Maven

```xml
<dependency>
  <groupId>io.github.darkona</groupId>
  <artifactId>logged</artifactId>
  <version>1.6.0</version>
</dependency>
```

Gradle (Groovy)

```groovy
dependencies {
    implementation "io.github.darkona:logged:1.6.0"
}
```

Spring Boot autoconfiguration wires everything by default.

---

## Quick Start

```java
@Logged
public String greet(String name) {
  return "Hello, " + name;
}
```

Example log output:

```
↓○ DemoService::greet called with args: [(String)name:Ana]
↑○ DemoService::greet returned with value: Hello, Ana Time taken: 1 ms
```

---

## @Logged Annotation Reference

The core of this library is the `@Logged` annotation. Place it on methods or classes (all public methods inside will be logged). It controls what is logged for method entry, arguments, return values, execution time, and exceptions.

### Options

| Attribute         | Default | Description                                  |
| ----------------- | ------- | -------------------------------------------- |
| `onCall`          | `true`  | Emit on method entry.                        |
| `args`            | `true`  | Include argument names/types.                |
| `argValues`       | `ALL`   | Values: `ALL`, `NONE`, `NULL`.               |
| `onReturn`        | `true`  | Emit on normal return.                       |
| `returnValue`     | `ALL`   | Log return value.                            |
| `onException`     | `true`  | Emit on exception.                           |
| `time`            | `true`  | Add duration.                                |
| `callMsg`         | `""`    | Entry template override.                     |
| `returnMsg`       | `""`    | Return template override.                    |
| `exceptionMsg`    | `""`    | Exception template override.                 |
| `level`           | `INFO`  | Base level for entry/return.                 |
| `exceptionLevel`  | `ERROR` | Level for exception.                         |
| `logStackTrace`   | `false` | Include stack trace in log.                  |
| `maskArgValues`   | `{}`    | Mask by argument name.                       |
| `maskAtPos`       | `{}`    | Mask by 0-based position.                    |
| `maskTypes`       | `{}`    | Mask by type.                                |
| `maskPatterns`    | `{}`    | Mask when `toString()` matches regex.        |
| `maskReturn`      | `false` | Mask return value.                           |
| `markers`         | `{}`    | Extra markers to attach.                     |
| `warnIfOverMs`    | `-1`    | Promote level if duration exceeds threshold. |
| `slowMarker`      | `""`    | Marker for slow calls.                       |

Enum `Values` for `argValues`/`returnValue`: `ALL`, `NONE`, `NULL`.

---

## Configuration (logged.\*)

### Core

```yaml
logged:
  enabled: true
  color: true
  icons: true
  useUtf8: true # opt-in (default false): replaces System.out/System.err JVM-wide
  callIcon: "↓○"
  returnIcon:  "↑○"
  exceptionIcon: "↑x"
  depthIcon: ">"
  maxValueLength: 2048
```

### Masking

```yaml
logged:
  maskString: "*****"
  maskLength: 5
  maskPatterns:
    - "\d{16}"
    - "(?i)secret|token"
  maskTypeNames:
    - "java.util.UUID"
  failOnInvalidMaskPatterns: false
  failOnUnresolvedMaskTypes: false
  maskReturn: false
```

### Thresholds

```yaml
logged:
  threshold:
    warnMs: 250
    promoteLevel: WARN
```

### Icon Themes

```yaml
logged:
  useIconTheme: true
  iconTheme: SJET_3
```

### SLF4J Plugin

```yaml
logged:
  slf4j:
    enabled: true
    color: true
    iconColors: true
    callMsgNoArgs: "{h:}{cI:} {c}::{m} called."
    callMsgArgs:   "{h:}{cI:} {c}::{m} called with args: [{a}]"
    returnMsg:     "{h:}{rI:} {c}::{m} returned."
    returnMsgValue:"{h:}{rI:} {c}::{m} returned with value: {rV}"
    timeTakenMsg:  "Time taken: {d} ms"
    logDepth: true
    captureFromMdc: [trace_id, span_id, call_id]
    markers: []
    maxValueLength: 2048
```

### MDC

```yaml
logged:
  mdc:
    enabled: true
    classKey: class
    methodKey: method
    traceKey: trace_id
```

### OpenTelemetry

```yaml
logged:
  otel:
    enabled: true
    mdcKey: trace
    attributes: {}
```

---

## Tokens Reference

Tokens inside templates:

* `{c}` Class simple name
* `{C}` Fully qualified class name
* `{m}` Method name
* `{t}` Return type
* `{a}` Arguments list
* `{rV}` Return value
* `{rC}` Return type class name
* `{d}` Duration (ms)
* `{f}` Exception origin file
* `{L}` Exception origin line
* `{ex}` Exception class name
* `{eM}` Exception message
* `{ec}` Exception origin class
* `{em}` Exception origin method
* `{eI}` Entry icon
* `{xI}` Exit icon
* `{tI}` Throw icon
* `{dI}` Depth icon
* `{h}` Depth string when enabled
* `{mdc.<key>}` Value from MDC

Supports defaults: `{token:default}`. Use `{{` and `}}` for escaping.

---

## Utilities

* **Bannerizer**: ASCII/Unicode banners, centering, tables, menus.
* **Colorizer**: ANSI colors, RGB, predefined palettes, reset.
* **Transformer**: `objectString`, `mask`, `truncate`, `capitalize`, substrings, safe string ops.
* **StringInterpolator**: generic token interpolation with map.
* **LogDecorator**: apply colors, banners, ornaments, case adjustments.

---

## Aspect Weaving

Spring AOP proxies only intercept public methods. To log private/self calls, use AspectJ weaving.

### Load-Time Weaving (LTW)

1. Add dependencies (`spring-boot-starter-aop`, `aspectjweaver`).
2. Create `META-INF/aop.xml` including your packages and `io.github.darkona.logged..*`.
3. Start JVM with `-javaagent:/path/to/aspectjweaver.jar`.

### Compile-Time Weaving (CTW)

Use `aspectj-maven-plugin` or a Gradle AspectJ plugin (e.g., FreeFair). Classes are woven at build time; no agent needed.

Both LTW and CTW:

* Log private/protected methods.
* Capture self-invocation.

---

## Examples

```java
// Default usage
@Logged
public String salute() { return "Hello World!"; }

// Disable entry logging
@Logged(onCall = false)
public String hello() { return "Hi"; }

// Mask arguments
@Logged(maskArgValues = {"password"}, maskAtPos = {1})
public void login(String username, String password) { }

// Custom templates
@Logged(callMsg = "{eI}{c}.{m} → args={a}",
        returnMsg = "{xI}{c}.{m} ⇒ {rV} ({d}ms)",
        exceptionMsg = "{tI}{c}.{m} !! {ex}: {eM} at {ec}.{em} ({f}:{L})")
public String greet(String name) { return "Hello, " + name; }
```

---

## Architecture

* Spring AOP or AspectJ weaving intercepts `@Logged` methods.
* `LoggedEngine` composes tokens and plugins.
* Plugins: SLF4J, MDC, OTel, color/decorator utilities.
* No heavy runtime reflection.

---

## License

Logged is licensed under **LGPL-3.0**. Free for commercial and open-source use. If you modify and distribute the library itself, you must publish your changes under the same license.
