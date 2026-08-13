# @Logged — Defaults and Options

This page documents how to use the `@Logged` annotation, its defaults, and all available options. 

---

## Overview

`@Logged` enables method-level logging (or class-level, affecting contained methods) via Spring AOP. It can log:

- Method entry and arguments
- Method return and return value
- Exceptions
- Execution duration
- Optional markers and custom messages
- Masking by name/position, by type, or by regex pattern

Works with SLF4J (Logback/Log4j2). Spring Boot autoconfiguration wires it by default.

---

## Defaults (out of the box)

Unless overridden, a method annotated with `@Logged`:

- Logs on entry, return, and exception.
- Includes argument names/types and their values.
- Logs the return value.
- Measures and logs execution time (ms).
- Uses levels: `INFO` for entry/return, `ERROR` for exceptions.
- Does not include stack traces (disabled by default).
- No per-method slow-call threshold (disabled with `-1`).
- No masking unless configured.
- No extra markers unless specified.

---

## Annotation Attributes

All attributes are optional and have sensible defaults. Apply `@Logged` on a method or on a class (class-level acts as default for contained methods; individual methods can override).

| Attribute | Type | Default | Description |
| --- | --- | --- | --- |
| `onCall` | `boolean` | `true` | Emit a log on method entry. |
| `args` | `boolean` | `true` | Include argument names and types. |
| `argValues` | `Values` | `ALL` | Controls argument value logging: `ALL`, `NONE`, `NULL` (only when null). |
| `onReturn` | `boolean` | `true` | Emit a log on normal return. |
| `returnValue` | `Values` | `ALL` | Controls return value logging: `ALL`, `NONE`, `NULL`. |
| `onException` | `boolean` | `true` | Emit a log when an exception occurs. |
| `time` | `boolean` | `true` | Include duration (ms) on return/exception. |
| `callMsg` | `String` | `""` | Custom entry message/template. |
| `returnMsg` | `String` | `""` | Custom return message/template. |
| `exceptionMsg` | `String` | `""` | Custom exception message/template. |
| `warnIfOverMs` | `long` | `-1` | Per-method slow-call threshold in ms; `< 0` disables (falls back to global if any). |
| `slowMarker` | `String` | `""` | Marker to attach when the call exceeds the active threshold. |
| `level` | `org.slf4j.event.Level` | `INFO` | Base level for entry/return logs. |
| `exceptionLevel` | `org.slf4j.event.Level` | `ERROR` | Level for exception logs. |
| `logStackTrace` | `boolean` | `false` | Include stack trace when logging exceptions. |
| `maskArgValues` | `String[]` | `{}` | Mask by argument name. |
| `maskAtPos` | `int[]` | `{}` | Mask by 0-based position. |
| `maskTypes` | `Class<?>[]` | `{}` | Mask when runtime type is assignable to any listed class. |
| `maskPatterns` | `String[]` | `{}` | Mask when `toString()` matches any regex. |
| `maskReturn` | `boolean` | `false` | Mask the return value in logs. |
| `markers` | `String[]` | `{}` | Extra markers to attach to all logs from this method. |

Enum for value control:

```java
enum Values { NONE, ALL, NULL }
```

Notes

- Type/pattern-based masking applies to argument values and to return values.
- When a threshold is active and exceeded, the engine may promote the effective level (see global config) and attach the `slowMarker` if set.

---

## Interaction With Global Configuration

Configure global behavior via `application.yaml` (prefix `logged`). Annotation attributes take precedence for that method when both exist.

Minimal example:

```yaml
logged:
  enabled: true
  color: true
  icons: true
  useUtf8: true
  maxValueLength: 2048

  # Masking
  maskString: "*****"
  maskLength: 5
  maskPatterns: []
  maskTypeNames: []
  failOnInvalidMaskPatterns: false
  failOnUnresolvedMaskTypes: false
  maskReturn: false

  # Thresholds
  threshold:
    warnMs: -1          # disable if < 0
    promoteLevel: WARN  # INFO < WARN < ERROR (slf4j)

  # Icons (optional, when not using themes)
  useIconTheme: false
  callIcon: ">>"
  returnIcon:  "<<"
  exceptionIcon: "!!"
  depthIcon: ">"
```

- `maxValueLength` truncates long values early (before plugins).
- `maskReturn` can be enabled globally and overridden per method.
- `threshold.warnMs` and `threshold.promoteLevel` define slow-call handling globally; `@Logged#warnIfOverMs >= 0` overrides per method.

---

## Usage Examples

Basic logging

```java
import io.github.darkona.logged.Logged;

@Logged
String greet(String name) {
  return "Hello, " + name;
}
```

Exit-only with duration (no entry log)

```java
@Logged(onCall = false, onReturn = true, time = true)
String compute() { /* ... */ }
```

Exceptions only (with stack trace)

```java
@Logged(onCall = false, onReturn = false, onException = true, logStackTrace = true)
void risky() { /* ... */ }
```

Hide all argument values

```java
@Logged(argValues = Logged.Values.NONE)
void createUser(String user, String password) { /* ... */ }
```

Mask by name and position

```java
@Logged(
  maskArgValues = {"password", "secret"},
  maskAtPos = {1} // second parameter
)
void login(String user, String password) { /* ... */ }
```

Mask by type and regex pattern

```java
@Logged(
  maskTypes = {java.util.UUID.class},
  maskPatterns = {"(?i)token|secret|pwd=\\w+"}
)
void updateToken(String token, java.util.UUID id) { /* ... */ }
```

Mask return value

```java
@Logged(maskReturn = true)
String getSensitive() { /* ... */ }
```

Custom messages and levels

```java
@Logged(
  callMsg = "Starting {c}::{m}",
  returnMsg = "Done {c}::{m}",
  exceptionMsg = "Boom in {c}::{m}",
  level = org.slf4j.event.Level.DEBUG,
  exceptionLevel = org.slf4j.event.Level.ERROR
)
String work() { /* ... */ }
```

Per-method slow-call threshold and marker

```java
@Logged(
  warnIfOverMs = 200,
  slowMarker = "SLOW_API"
)
String fetchRemote() { /* ... */ }
```

Attach additional markers

```java
@Logged(markers = {"AUDIT", "PAYMENTS"})
void charge(/* ... */) { /* ... */ }
```

Class-level defaults with method overrides

```java
@Logged(level = DEBUG, argValues = Logged.Values.NONE)
class UserService {

  // Inherits DEBUG and no arg values
  public void create(/* ... */) { /* ... */ }

  // Override for this method
  @Logged(argValues = Logged.Values.ALL, level = INFO)
  public void find(/* ... */) { /* ... */ }
}
```

---

## Best Practices

- Use `argValues = NONE` on public-facing boundaries; enable `ALL` only where risk is low.
- Prefer `maskTypes` for sensitive classes (credentials, tokens) and complement with `maskPatterns` for strings.
- Keep `maskPatterns` values as compile-time constants. Compiled patterns are kept in a bounded LRU cache (`logged.cache.max-annotation-patterns`, default 512); generating patterns dynamically churns the cache and forces repeated recompilation.
- Enable a global `threshold.warnMs` (e.g., 250 ms) and fine-tune with `warnIfOverMs` for critical paths.
- Annotate at class level for consistent defaults; override only where needed.
- Turn on `logStackTrace` selectively where stacks add value and won’t overwhelm logs.

---

## Code References

- Annotation definition: `src/main/java/io/github/darkona/logged/Logged.java:1`
- Global properties: `src/main/java/io/github/darkona/logged/LoggedProperties.java:1`

---

## Precedence & Repeatability

- Precedence: a method-level `@Logged` overrides a class-level `@Logged` for that method. Only one set of options is applied.
- Not repeatable: `@Logged` is not marked `@Repeatable`, and the engine reads a single annotation (`method` if present, else `class`). You cannot stack two `@Logged` on the same element to emit multiple logs (e.g., INFO and DEBUG at once).
- If you need multiple outputs/levels from a single call, consider emitting an additional manual log in your method, or open a feature request to discuss a repeatable/compound design.
