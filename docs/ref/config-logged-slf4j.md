# Logged + SLF4J — Base Configuration

This page explains the base configuration for the Logged library using the default SLF4J plugin. All examples use Spring Boot application.yaml keys. Replace with application.properties as needed.

---

## Overview

- Autoconfiguration enables Logged and the SLF4J plugin by default.
- Use `logged.*` for core engine settings, masking, icons, and thresholds.
- Use `logged.slf4j.*` to customize SLF4J message templates, colors, and behavior.

---

## Minimal Setup

```yaml
logged:
  enabled: true
  color: true          # color decorator enabled
  icons: true          # include icons in messages
  useUtf8: true        # ensure UTF-8 for pretty symbols (System.out)
  maxValueLength: 2048 # early truncation of values

  # Masking (global)
  maskString: "*****"
  maskLength: 5
  maskPatterns: []
  maskTypeNames: []
  failOnInvalidMaskPatterns: false
  failOnUnresolvedMaskTypes: false
  maskReturn: false

  # Threshold (global)
  threshold:
    warnMs: -1          # disable if < 0
    promoteLevel: WARN  # INFO < WARN < ERROR

  # Icons (ASCII by default)
  callIcon: ">>"
  returnIcon: "<<"
  exceptionIcon: "!!"
  depthIcon: ">"

  # SLF4J plugin (default on)
  slf4j:
    enabled: true
    color: true
    iconColors: true
    callMsgNoArgs: "{h:}{cI:} {c}::{m} called."
    callMsgArgs:   "{h:}{cI:} {c}::{m} called with args: [{a}]"
    returnMsg:     "{h:}{rI:} {c}::{m} returned."
    returnMsgValue: "{h:}{rI:} {c}::{m} returned with value: {rV}"
    exceptionMsg:  "{h:}{exI:} {c}::{m} threw a {ex}: {eM} at {ec}.{em} ({f}:{L})"
    timeTakenMsg:  "Time taken: {d} ms"
    argsTemplate:  "({c}){k}:{v}"
    logDepth: true
    captureFromMdc: [trace_id, span_id, call_id]
    markers: []
    maxValueLength: 2048
```


## Examples

Return-only with value and timing
```yaml
logged:
  slf4j:
    returnMsg: "{h:}{rI:} {c}::{m} returned. {time}"
    returnMsgValue: "{h:}{rI:} {c}::{m} returned: {rV} ({d} ms)"
```

Add MDC trace context
```yaml
logged:
  slf4j:
    captureFromMdc: [trace_id, span_id]
    callMsgArgs: "{h:}[trace={mdc.trace_id}] {c}::{m} args: [{a}]"
```

Strict masking policy
```yaml
logged:
  maskReturn: true
  maskPatterns:
    - "(?i)token|secret|pwd=\w+"
  maskTypeNames:
    - "java.util.UUID"
```

Slow-call promotion (global)
```yaml
logged:
  threshold:
    warnMs: 250
    promoteLevel: WARN
```

---

## Gotchas

- Ensure your console/file appenders use UTF‑8 if you use non-ASCII icons.
- Masking happens before formatting; choose `maskString`/`maskLength` suitable for your logs.
- If you customize templates heavily, validate them at startup (a missing token won’t fail, but might render oddly).
- For performance-sensitive paths, keep `args = false` or `argValues = NONE`, and prefer short templates.

---

## Full Reference — `logged.*`

- enabled: boolean, default true: master on/off switch for the engine.
- announceLoad: boolean, default true: prints a short banner when the engine/plugins load.
- color: boolean, default true: enables color decorator for library-provided templates.
- icons: boolean, default true: enables icon injection into tokens/templates.
- useUtf8: boolean, default true: forces `System.out` to UTF‑8 for correct icon rendering when possible.
- maxValueLength: int, default 2048: early truncation length for argument and return values (applies before plugins).

Masking
- maskString: string, default "*****": string used to mask sensitive values.
- maskLength: int, default 5: length of the mask string included in logs.
- maskPatterns: list<string>, default []: regex patterns; if a value’s string matches, it is masked.
- maskTypeNames: list<string>, default []: fully-qualified class names; if a value is assignable to any, it is masked.
- failOnInvalidMaskPatterns: boolean, default false: fail startup when a pattern in `maskPatterns` is invalid.
- failOnUnresolvedMaskTypes: boolean, default false: fail startup when a class in `maskTypeNames` cannot be loaded.
- maskReturn: boolean, default false: mask return values globally (can be overridden per-method via `@Logged(maskReturn=true)`).

Icons & Themes
- callIcon: string, default ">>": icon for call/entry events.
- returnIcon: string, default "<<": icon for return/exit events.
- exceptionIcon: string, default "!!": icon for exceptions.
- depthIcon: string, default ">": icon used to render nesting depth.
- useIconTheme: boolean, default false: when true, icons come from `iconTheme` instead of raw strings.
- iconTheme: enum, optional: predefined theme from `io.github.darkona.logged.utils.SymbolTheme`.

Threshold
- threshold.warnMs: long, default -1: global slow-call threshold in ms; `< 0` disables.
- threshold.promoteLevel: org.slf4j.event.Level, default WARN: level to promote slow calls to; effective level is max(base, promote).

Stack Frames
- stack.skip-prefixes: list<string>, default `[java., jdk., sun., org.springframework., org.aspectj., io.github.darkona.logged.]`: class-name prefixes skipped when resolving the exception origin frame (`{ec}`, `{em}`, `{L}`, `{f}` tokens). Setting this property replaces the defaults, so include them if you only want to append.

Caches
- cache.max-annotation-patterns: int, default 512: maximum number of compiled `@Logged(maskPatterns=...)` regexes retained in an LRU cache (invalid patterns are negatively cached). Mask patterns should be compile-time constants; avoid generating them dynamically.

---

## Full Reference — `logged.slf4j.*`

Enable & Behavior
- enabled: boolean, default true: turn the SLF4J plugin on/off.
- color: boolean, default true: enable colors in SLF4J output.
- iconColors: boolean, default true: tint icons independently using color settings below.
- logDepth: boolean, default true: add visual depth indicator and shift by nesting level.
- maxValueLength: int, default 2048: final guard for truncating values at emission time.

Templates
- callMsgNoArgs: string, default "{h:}{cI:} {c}::{m} called.": template for call/entry when there are no arguments.
- callMsgArgs: string, default "{h:}{cI:} {c}::{m} called with args: [{a}]": template for call with args.
- returnMsg: string, default "{h:}{rI:} {c}::{m} returned.": template when method returns and value is not shown.
- returnMsgValue: string, default "{h:}{rI:} {c}::{m} returned with value: {rV}": template when value is shown.
- exceptionMsg: string, default "{h:}{exI:} {c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})": template for exceptions.
- timeTakenMsg: string, default "Time taken: {d} ms": snippet appended when `@Logged(time=true)`.
- argsTemplate: string, default "({c}){k}:{v}": per-argument template used to build `{a}`.

MDC & Markers
- captureFromMdc: list<string>, default []: MDC keys to bring into the token map (address as `{mdc.<key>}` in templates).
- markers: list<string>, default []: markers to attach to SLF4J events (merged with annotation-level `@Logged(markers=...)`).
- captureFromOtel: list<string>, default []: keys to forward from OpenTelemetry context into the token map (when Otel plugin is active).

Icon Colors
- callIconColor: string, default BLUE: icon tint for call messages.
- returnIconColor: string, default GREEN: icon tint for return messages.
- exceptionIconColor: string, default RED: icon tint for exception messages.
- depthIconColor: string, default ORANGE: icon tint for depth indicator.

Key-Value Emission (optional)
- keyValue: boolean, default false: when true, the plugin also emits selected tokens as structured key-values.
- keyValues: list<LogToken>, default [ARGUMENTS, METHOD_NAME, METHOD_TYPE, RETURN_VALUE, DURATION]: which tokens to include.

Notes
- Annotation-level templates (`callMsg`, `returnMsg`, `exceptionMsg`) override plugin templates per invocation.
- For return logging, the effective template depends on `@Logged(returnValue)`:
  - ALL → `returnMsgValue`
  - NULL → use `returnMsgValue` only when the runtime return string equals `null`, otherwise `returnMsg`
  - NONE → no return message

