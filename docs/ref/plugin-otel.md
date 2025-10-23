# OpenTelemetry Plugin — Full Configuration

This page documents the OpenTelemetry plugin, which creates INTERNAL spans around `@Logged` method calls and enriches them with code/args metadata. It can optionally write the span name to MDC.

- Source: `src/main/java/io/github/darkona/logged/plugins/otel/LoggedOpenTelemetryPlugin.java:1`
- Properties: `src/main/java/io/github/darkona/logged/plugins/otel/LoggedOpenTelemetryProperties.java:1`
- Auto-config: `src/main/java/io/github/darkona/logged/plugins/otel/LoggedOpenTelemetryConfig.java:1`

---

## Overview

- Creates a span per intercepted method with kind INTERNAL.
- Names the span using a template over Logged tokens (class, method, etc.).
- Adds code semantic attributes and optional Logged-specific attributes.
- Respects `@Logged` flags (`onCall`, `onReturn`, `onException`, `args`, `time`).
- Manages a per-thread span stack for proper nesting.

---

## Defaults At A Glance

- `logged.otel.enabled`: false (opt-in)
- `logged.otel.addSourceLine`: true (adds file name and line)
- `logged.otel.addClass`: true (adds code.namespace)
- `logged.otel.addMethod`: true (adds code.function)
- `logged.otel.addDepth`: true (adds logged.depth)
- `logged.otel.addArgs`: true (adds args count, names, masked names)
- `logged.otel.addReturnType`: true (on return)
- `logged.otel.addExceptionMsg`: true (status description on error)
- `logged.otel.addToMdc`: true (writes span name to MDC)
- `logged.otel.mdcKey`: `span_name`
- `logged.otel.spanIdTemplate`: `Logged: {c}${m}()`

---

## Configuration (`logged.otel.*`)

- enabled: boolean, default false
  - Turns the plugin on/off. Requires `io.opentelemetry:opentelemetry-api` on classpath.
- addSourceLine: boolean, default true
  - Records file name (`code.filepath`) and line (`code.lineno`) when available.
- addClass: boolean, default true
  - Records `code.namespace`.
- addMethod: boolean, default true
  - Records `code.function`.
- addDepth: boolean, default true
  - Records `logged.depth` (nesting depth of @Logged interceptions).
- addArgs: boolean, default true
  - Records `logged.args.count`, `logged.args.names`, and `logged.args.masked` (names of masked args by name/position).
- addReturnType: boolean, default true
  - On return, records `logged.return.type`. Also sets `logged.return.null=true` when the reported return type string is `null`.
- addExceptionMsg: boolean, default true
  - On exception, sets StatusCode.ERROR and includes a short description (exception.toString()) when true.
- addToMdc: boolean, default true
  - Writes the computed span name into MDC under `mdcKey`; cleared on method completion.
- mdcKey: string, default `span_name`
  - MDC key used when `addToMdc=true`.
- spanIdTemplate: string, default `Logged: {c}${m}()`
  - Template for span name; tokens come from the Logged token map (see below). Processed by the StringInterpolator.

Example
```yaml
logged:
  otel:
    enabled: true
    addSourceLine: true
    addClass: true
    addMethod: true
    addDepth: true
    addArgs: true
    addReturnType: true
    addExceptionMsg: true
    addToMdc: true
    mdcKey: span_name
    spanIdTemplate: "{C}#{m}"
```

---

## Span Attributes

The plugin records OpenTelemetry semantic code attributes plus custom `logged.*` attributes.

- code.namespace: fully-qualified class name.
- code.function: method name.
- code.filepath: source file name (when available).
- code.lineno: 1-based line number (when available).
- logged.depth: nesting depth for @Logged interceptions.
- logged.args.count: number of arguments observed.
- logged.args.names: ordered list of argument names.
- logged.args.masked: names of arguments whose values are masked by name/position.
- logged.return.type: simple name of the runtime return type (when enabled on return).
- logged.return.null: true when the return type string equals `null`.

Status and errors
- onReturn: sets StatusCode.OK.
- onException: records exception, sets StatusCode.ERROR (with description when addExceptionMsg=true).

---

## Span Naming (template tokens)

`spanIdTemplate` can use any of the tokens in the Logged token map. Common tokens:
- {c}: simple class name
- {C}: fully-qualified class name
- {m}: method name
- {t}: declared return type (simple name)
- {rC}: runtime return type (simple name)
- {a}: printable arguments (only meaningful on call)
- {d}: duration (meaningful on return/exception)
- {f}:{L}: file and line (when available)

Example
```yaml
logged:
  otel:
    spanIdTemplate: "{C}#{m}({a})"
```

---

## Threading & Nesting

- A per-thread span stack ensures proper nesting when methods annotated with @Logged call other @Logged methods.
- Each onCall pushes a span; onReturn/onException end and pop. The stack is removed when empty.

---

## Interaction With Other Plugins

- MDC: when `addToMdc=true`, the span name is added to MDC (`mdcKey`) and cleared after completion.
- SLF4J: you can include the span name in templates via `{mdc.<mdcKey>}` when `captureFromMdc` is configured in the SLF4J plugin.

---

## Tips & Gotchas

- Ensure an OpenTelemetry SDK is configured to export spans (e.g., the Java agent or manual SDK wiring). This plugin emits spans; it does not configure export.
- Source location (file/line) depends on AspectJ’s ability to resolve `ProceedingJoinPoint#getSourceLocation()` in your runtime.
- Argument names depend on compilation with `-parameters` to be accurate.
- Sensitive data: values are masked/truncated by the engine before reaching this plugin; adjust masking settings under `logged.*` or per-method.
