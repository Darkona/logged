# MDC Plugin — Full Configuration

This page documents the Logged MDC plugin, which writes per-invocation metadata to SLF4J MDC and clears it on completion.

- Source: `src/main/java/io/github/darkona/logged/plugins/mdc/LoggedMdcPlugin.java:1`
- Properties: `src/main/java/io/github/darkona/logged/plugins/mdc/LoggedMdcProperties.java:1`

---

## Overview

- Purpose: enrich log events with contextual keys in SLF4J MDC for easier filtering/formatting.
- Lifecycle: onCall → onReturn/onException → afterMethod (cleans up its own keys).
- Scope: MDC is thread-local. The plugin removes only the keys it manages to avoid leaking context.

---

## Defaults At A Glance

- `logged.mdc.enabled`: false (opt-in)
- `logged.mdc.addMark`: false
- `logged.mdc.markerKey`: `log_source`
- `logged.mdc.markerValue`: `logged`
- `logged.mdc.argsTemplate`: `({c}){k}:{v}`
- `logged.mdc.maxValueLength`: 2048

---

## Configuration (`logged.mdc.*`)

- enabled: boolean, default false
  - Turns the MDC plugin on/off. Keep false if you don’t use MDC in your logging pattern.
- addMark: boolean, default false
  - When true, puts a static marker key/value in MDC during the call (removed on completion).
- markerKey: string, default `log_source`
  - MDC key used by the static mark (only when addMark = true).
- markerValue: string, default `logged`
  - MDC value used by the static mark (only when addMark = true).
- argsTemplate: string, default `({c}){k}:{v}`
  - Format for individual arguments composing the `{args}` MDC value. Available tokens: `{c}` (type), `{k}` (name), `{v}` (value).
- maxValueLength: int, default 2048
  - Final guard for truncating values placed into MDC (arguments and return).

Example
```yaml
logged:
  mdc:
    enabled: true
    addMark: true
    markerKey: log_source
    markerValue: logged
    argsTemplate: "({c}){k}:{v}"
    maxValueLength: 2048
```

---

## MDC Keys Written

On call (when `@Logged(onCall=true)`):
- `class`: simple/qualified class name (engine supplies `CLASS_LONG`).
- `method`: method name.
- `method_type`: declared return type simple name.
- `args`: printable argument list when `@Logged(args=true)`.
- `[markerKey]`: static mark when `addMark=true`.

On return (always writes `outcome=ok`):
- `result`: printable return value when `@Logged(onReturn=true)`.
- `result_type`: runtime return type (`RETURN_CLASS`).
- `latency_ms`: execution time when `@Logged(time=true)`.

On exception (always writes `outcome=error`):
- `exception`: exception simple class name.
- `exception_msg`: exception message when `@Logged(onException=true)`.

Cleanup:
- After each method (return or exception), removes only the keys managed by this plugin.

---

## How It Interacts With @Logged

- Respects `onCall`, `onReturn`, `onException`, `args`, and `time` from the annotation per invocation.
- Uses the engine’s masked/truncated representations (values are already masked according to global/annotation rules).
- Annotation overrides (e.g., `argValues`, `maskReturn`) affect what ends up in MDC.

---

## Logging Pattern Examples

Logback pattern using MDC keys
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{log_source}] %X{class}.%X{method} outcome=%X{outcome} %msg%n
```

JSON encoder (conceptual)
- Use your encoder’s MDC inclusion feature to add `class`, `method`, `args`, `result`, `outcome`, `exception`, `latency_ms`.

---

## Tips & Gotchas

- Thread reuse: app servers reuse threads; this plugin clears only its own keys to avoid clobbering your trace/correlation IDs.
- Argument names: compile with `-parameters` to retain parameter names; otherwise names may be synthesized by the engine.
- Masking: sensitive data is masked before reaching this plugin; adjust global `logged.*` masking or annotation options as needed.
- Performance: MDC puts are O(1) but still add overhead; disable `args` and/or return recording if you need to minimize write volume.
- Tracing: prefer the OpenTelemetry plugin to populate trace/span IDs; you can then surface them via `%X{trace_id}` in patterns.

---

## Complete YAML Example

```yaml
logged:
  # engine basics
  enabled: true
  color: true
  icons: true
  useUtf8: true

  # masking policy
  maskString: "*****"
  maskLength: 5
  maskPatterns: ["(?i)token|secret|pwd=\\w+"]
  maskTypeNames: ["java.util.UUID"]

  # mdc plugin
  mdc:
    enabled: true
    addMark: true
    markerKey: log_source
    markerValue: logged
    argsTemplate: "({c}){k}:{v}"
    maxValueLength: 2048

  # slf4j plugin (pair nicely with MDC)
  slf4j:
    enabled: true
    callMsgArgs:   "{h:}{cI:} {c}::{m} args: [{a}]"
    returnMsg:     "{h:}{rI:} {c}::{m} returned."
    returnMsgValue:"{h:}{rI:} {c}::{m} returned: {rV}"
    exceptionMsg:  "{h:}{exI:} {c}::{m} threw {ex}: {eM}"
```

---

## MDC Keys Table

| Key | Description | Example | Notes |
| --- | --- | --- | --- |
| `log_source` | Static marker when `addMark=true` | `logged` | Key/value configurable via `markerKey`/`markerValue`. |
| `class` | Fully-qualified class name | `com.example.DemoService` | Source class of the invocation. |
| `method` | Method name | `findUser` | Intercepted method. |
| `method_type` | Declared return type (simple name) | `String` | From signature. |
| `args` | Printable argument list | `[(String)id:42]` | Honours `argValues`, masking and `argsTemplate`. |
| `result` | Printable return value | `User{id=42,...}` | Present when `onReturn=true`; masked/truncated as configured. |
| `result_type` | Runtime return type (simple name) | `User` | From runtime object. |
| `outcome` | Result of invocation | `ok` or `error` | Set on return/exception. |
| `exception` | Exception class (simple name) | `IllegalArgumentException` | Present on exception. |
| `exception_msg` | Exception message | `invalid id` | Present when `onException=true`. |
| `latency_ms` | Duration in milliseconds | `27` | Present when `time=true`. |
| `trace_id` | Trace identifier (from Otel plugin) | `8f3a...` | If using OpenTelemetry plugin. |
| `span_id` | Span identifier (from Otel plugin) | `4cd2...` | If using OpenTelemetry plugin. |

---

## Patterns — Logback

Classic pattern including MDC keys
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{log_source}] %X{class}.%X{method} type=%X{method_type} outcome=%X{outcome} latency=%X{latency_ms}ms %msg%n
```

Route by marker (example marker filter already included in this repo)
```xml
<!-- logback-spring.xml excerpt -->
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
  <encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{log_source}] %X{class}.%X{method} %msg%n</pattern>
  </encoder>
</appender>
```

JSON with logstash-logback-encoder (optional dependency)
```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
  <scope>runtime</scope>
  <!-- choose a version compatible with your stack -->
</dependency>

<!-- logback-spring.xml excerpt -->
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
    <providers>
      <timestamp>
        <timeZone>UTC</timeZone>
      </timestamp>
      <logLevel/>
      <loggerName/>
      <threadName/>
      <message/>
      <mdc/>
      <stackTrace/>
    </providers>
  </encoder>
  <!-- optional: attach a marker filter here -->
</appender>
```

---

## Patterns — Log4j2

Classic PatternLayout including MDC keys
```xml
<PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%X{log_source}] %X{class}.%X{method} type=%X{method_type} outcome=%X{outcome} latency=%X{latency_ms}ms %m%n"/>
```

JSONLayout including MDC map
```xml
<JSONLayout complete="false" compact="true" properties="true" eventEol="true"/>
<!-- properties=true includes MDC as the 'contextMap' object -->
```

Routing by marker (Log4j2)
```xml
<Filters>
  <MarkerFilter marker="NO_CONSOLE" onMatch="DENY" onMismatch="NEUTRAL"/>
</Filters>
```
