# Logged

Logging that feels like a teammate: clear, flexible, and right where you need it. Logged is a Spring Boot library that uses AOP to log method entry, returns, exceptions, and timing — with templates, redaction, plugins, and optional weaving for private/self calls. Think of it as a careful scribe that keeps your code’s story tidy and useful.

Compatible with Java 21, Spring Boot 3.x, and SLF4J backends (Logback/Log4j2).

## Highlights

- Per-method or class-level logging via `@Logged`
- Arguments and returns with redaction, truncation, and templates
- Timing and thresholds: auto-promote slow calls and add markers
- Plugins: SLF4J formatting, MDC enrichment, OpenTelemetry spans
- Icon themes or manual icons, ANSI colors, and depth indicators
- Optional AspectJ weaving to cover private and self-invoked methods

## Installation

Maven

```xml
<dependency>
  <groupId>io.github.darkona</groupId>
  <artifactId>logged</artifactId>
  <version>1.3.0</version>
</dependency>
```

Gradle (Groovy)

```groovy
dependencies {
  implementation "io.github.darkona:logged:1.3.0"
}
```

Spring Boot autoconfiguration wires everything by default.

## Quick Start

```java
@Logged
public String greet(String name) {
  return "Hello, " + name;
}
```

Example log output (SLF4J plugin):

```
<eI> DemoService::greet called with args: [(String)name:Ana]
<xI> DemoService::greet returned with value: Hello, Ana  Time taken: 1 ms
```

Where `<eI>`/`<xI>` are icons resolved from your configuration.

## @Logged Options (per method or class)

| Attribute | Default | Description |
|---|---|---|
| `onCall` | `true` | Emit on method entry. |
| `args` | `true` | Include argument names/types. |
| `argValues` | `ALL` | Values: `ALL`, `NONE`, `NULL`. |
| `onReturn` | `true` | Emit on normal return. |
| `returnValue` | `ALL` | Log return value (modes above). |
| `onException` | `true` | Emit on exception. |
| `time` | `true` | Add duration to messages. |
| `callMsg` | `""` | Entry template override. |
| `returnMsg` | `""` | Return template override. |
| `exceptionMsg` | `""` | Exception template override. |
| `level` | `INFO` | Base level for entry/return. |
| `exceptionLevel` | `ERROR` | Level for exception. |
| `logStackTrace` | `false` | Include throwable in log call. |
| `redactArgValues` | `{}` | Redact by argument name. |
| `redactAtPos` | `{}` | Redact by 0-based position. |
| `redactTypes` | `{}` | Redact when argument type is assignable to any listed class. |
| `redactPatterns` | `{}` | Redact when `toString()` matches any regex. |
| `maskReturn` | `false` | Redact return value for this method. |
| `markers` | `{}` | Extra markers to attach. |
| `warnIfOverMs` | `-1` | Promote level if duration exceeds this threshold (disabled with `-1`). |
| `slowMarker` | `""` | Marker added when threshold is exceeded. |

`Values` enum for `argValues`/`returnValue`: `ALL`, `NONE`, `NULL`.

## Configuration (logged.*)

Core

```yaml
logged:
  enabled: true
  color: true
  icons: true
  useUtf8: true
  # Icons (manual, if you don’t use themes)
  entryIcon: ">>"
  exitIcon:  "<<"
  throwIcon: "!!"
  depthIcon: ">"
  maxValueLength: 2048
```

Redaction (global)

```yaml
logged:
  redactMask: "***"
  redactLength: 5
  # Regex patterns applied to argument/return string values
  redactPatterns:
    - "\d{16}"           # 16 digits (e.g., credit cards)
    - "(?i)secret|token"
  # Fully qualified class names; values assignable to these are masked
  redactTypeNames:
    - "java.util.UUID"
  # Make startup strict (optional)
  failOnInvalidRedactPatterns: false
  failOnUnresolvedRedactTypes: false
  # Mask all return values globally (can be overridden per method)
  maskReturn: false
```

Thresholds (auto-promote and marker)

```yaml
logged:
  threshold:
    warnMs: 250
    promoteLevel: WARN     # INFO/DEBUG/WARN/ERROR
```

Behavior: if `{d}` (duration) exceeds `warnMs`, Logged promotes the level to the higher of base vs `promoteLevel`, and adds `slowMarker` from the annotation when present.

### Icon themes

Prefer presets over manual strings? Enable a theme:

```yaml
logged:
  useIconTheme: true
  iconTheme: SJET_3  # any value from io.github.darkona.logged.utils.SymbolTheme
```

Precedence: when `useIconTheme=true` and `iconTheme` is set, the theme icons are used. To override icons manually instead, set `useIconTheme=false` and configure `entryIcon`/`exitIcon`/`throwIcon`/`depthIcon`.

## SLF4J Plugin (formatting & templates)

```yaml
logged:
  slf4j:
    enabled: true
    color: true
    iconColors: true
    callMsgNoArgs: "{h:}{eI:} {c}::{m} called."
    callMsgArgs:   "{h:}{eI:} {c}::{m} called with args: [{a}]"
    exitMsg:       "{h:}{xI:} {c}::{m} returned."
    exitMsgValue:  "{h:}{xI:} {c}::{m} returned with value: {rV}"
    timeTakenMsg:  "Time taken: {d} ms"
    logDepth: true
    captureFromMdc: [trace_id, span_id, call_id]
    markers: []
    maxValueLength: 2048
```

## MDC / OpenTelemetry

```yaml
logged:
  mdc:
    enabled: true
  otel:
    enabled: true
```

Notes
- Add `%X{key}` in your backend pattern/encoder to print MDC values.
- If you run with the OpenTelemetry Java Agent (or OTel appenders), `trace_id` and `span_id` are injected into MDC automatically. Logged’s OTel plugin focuses on span attributes and an optional span name in MDC (`logged.otel.mdcKey`).

## Tokens Reference

Use tokens inside templates (`callMsg`, `returnMsg`, `exceptionMsg`, and SLF4J templates). Tokens use `{key}` with optional defaults `{key:default}`. `{h:}` prints the depth string only when present.

Core tokens

| Token | Meaning |
|---|---|
| `{c}` | Class simple name |
| `{C}` | Class fully qualified name |
| `{m}` | Method name |
| `{t}` | Method return type |
| `{a}` | Printable argument list (built with `logged.slf4j.argsTemplate`) |
| `{rV}` | Return value (honors masking/truncation/mode) |
| `{rC}` | Return value class simple name |
| `{d}` | Duration in milliseconds |
| `{f}` | Source file name of exception origin |
| `{L}` | Source line number of exception origin |
| `{ex}` | Exception class simple name |
| `{eM}` | Exception message |
| `{ec}` | Exception origin class name |
| `{em}` | Exception origin method name |

Icons and depth

| Token | Meaning |
|---|---|
| `{eI}` | Entry icon |
| `{xI}` | Exit icon |
| `{tI}` | Throw icon |
| `{dI}` | Depth icon |
| `{h}`  | Visual depth string (e.g., `>>>`) when `logged.slf4j.logDepth=true` |

MDC tokens

- Use `{mdc.<key>}` to render a value captured from MDC, e.g. `{mdc.trace_id}`.

Arguments template tokens (only inside `argsTemplate`)

- `{c}`: argument type (simple name)
- `{k}`: argument name
- `{v}`: argument value (honors `argValues` and truncation)

## Interpolation & Templates

`StringInterpolator` powers token replacement:

- Strict: `StringInterpolator.interpolateStrict` throws if any token is missing.
- Defaultable: `interpolateWithDefaults` supports `{key:default}` and uses the default when the key is absent.
- Escaping: use `{{` to render `{` and `}}` to render `}`.
- Performance: a small in-memory LRU cache of compiled templates reduces parsing overhead on hot paths.

## Examples

Custom templates

```java
@Logged(callMsg = "{eI} {c}.{m} · args={a}",
        returnMsg = "{xI} {c}.{m} → {rV} ({d}ms)",
        exceptionMsg = "{tI} {c}.{m} !! {ex}: {eM} at {ec}.{em} ({f}:{L})")
public String greet(String name) { return "Hello, " + name; }
```

Redaction

```java
@Logged(redactArgValues = {"password", "token"}, redactAtPos = {1})
void login(String user, String password, String token) { }

@Logged(redactTypes = {java.util.UUID.class})
void load(UUID id) { }

@Logged(redactPatterns = {"\n|\r", "(?i)secret"})
void comment(String text) { }

@Logged(maskReturn = true)
String secret() { return "TopSecret"; }
```

Thresholds (promote + marker)

```java
@Logged(warnIfOverMs = 500, slowMarker = "SLOW")
void maybeSlow() { /* ... */ }
```

Class-level

```java
@Logged(level = org.slf4j.event.Level.DEBUG, returnValue = Logged.Values.NONE)
class DemoService { /* all public methods logged */ }
```

## Aspect Weaving (optional)

Spring AOP (proxy-based) intercepts public methods only and won’t capture private or self-invoked calls. If you need those, enable load-time weaving (LTW) with AspectJ and let Logged’s bridge delegate to the same engine.

What you get
- Logs for private methods
- Logs for self-invocation (method A calling method B on the same instance)

How it works
- Logged ships an AspectJ aspect: `io.github.darkona.logged.weaving.WeavedAspect`.

### Aspect Weaving (LTW) — Details

1) Add AspectJ weaver dependency and ensure `META-INF/aop.xml` is on the runtime classpath.

2) Configure `aop.xml` to include the Logged aspect:

```xml
<!DOCTYPE aspectj PUBLIC "-//AspectJ//DTD ASPECTJ 1.5.0//EN" "https://www.eclipse.org/aspectj/dtd/aspectj_1_5_0.dtd">
<aspectj>
  <aspects>
    <aspect name="io.github.darkona.logged.weaving.WeavedAspect"/>
  </aspects>
  <weaver options="-verbose">
    <include within="your.app.package..*"/>
    <include within="io.github.darkona.logged..*"/>
  </weaver>
  <runtime/>
</aspectj>
```

3) Start the JVM with the agent:

- Java: `-javaagent:/path/to/aspectjweaver.jar`
- Spring Boot Gradle `bootRun`: `jvmArgs "-javaagent:${configurations.runtimeClasspath.find { it.name.contains('aspectjweaver') }}"`

Detection
- The bridge enables when it detects the agent and the `aop.xml` resource with the aspect FQN.

Troubleshooting
- No logs for private/self calls? Verify `aop.xml` on classpath, agent present, include scopes correct; enable `-verbose` weaving and inspect startup output.

## Architecture

- `@Logged` + Spring AOP (or AspectJ LTW) capture calls
- `LoggedEngine` assembles tokens and coordinates plugins
- Plugins (SLF4J, MDC, OpenTelemetry, Logback/Log4j2 helpers) render/augment output

## Utilities & Colors (quick tour)

Helpers in `io.github.darkona.logged.utils` make output readable and playful when needed:

- Colorizer: true‑color foreground/background, named colors, rainbow effect, ANSI reset.
- Bannerizer: banners, centered text, simple key/value tables, menus.
- Transformer: `objectString`, `truncate`, `mask`, `capitalize`, and other low‑alloc helpers.

Examples

```java
// Colors
deco.custom(255, 54, 116, "Fuchsia");
deco.custom(io.github.darkona.logged.colors.BasicColor.BLUE, "Blue");

// Banner
System.out.println(io.github.darkona.logged.utils.Bannerizer.bannerize("Logged Init", 40));

// Transform
var txt = io.github.darkona.logged.utils.Transformer.objectString(value);
txt = io.github.darkona.logged.utils.Transformer.truncate(txt, 128);
var masked = io.github.darkona.logged.utils.Transformer.mask("supersecret", 4, '*');
```

## License

LGPL‑3.0 (see `LICENSE.md`). Use freely in commercial or open‑source projects. If you modify and distribute the library itself, publish the changes under the same license.

---

If documentation is a map, Logged tries to be the compass: precise enough to trust, light enough to carry. Have fun logging.
