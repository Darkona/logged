# Public API — Package `io.github.darkona.logged.api`

This page documents the public API for extending Logged: plugin SPI, token model, decorators, and helper types.

- Package: `src/main/java/io/github/darkona/logged/api/`
  - `LoggedPlugin` — plugin SPI (lifecycle hooks)
  - `Data` — per-invocation data (tokens, args, timing, depth)
  - `Arg` — argument view and rendering helper
  - `LogToken` — well-known tokens and their short keys
  - `LogDecorator` — formatting/color adapter injected into plugins

---

## `LoggedPlugin` — Plugin SPI

Implement this interface and register it as a Spring bean to participate in the logging lifecycle. The engine autowires `List<LoggedPlugin>` and calls them in order.

Hooks
- `onCall(ProceedingJoinPoint, Data, Logged)` — before method proceeds (after Data is assembled). Respect `options.onCall()` and `options.args()` if you emit messages.
- `onReturn(ProceedingJoinPoint, Data, Logged)` — after successful return. Respect `options.onReturn()` and `options.time()`.
- `onException(ProceedingJoinPoint, Data, Logged, Throwable)` — after an exception. Respect `options.onException()` and `options.logStackTrace()` as relevant.
- `onLoad()` — called once at startup (safe place to initialize resources).
- `announceLoad()` — return a short banner; the engine logs it if `logged.announceLoad=true`.
- `afterMethod()` — called in a finally block after each call (cleanup; clear thread-locals/MDC if you set them).

Registering a plugin
- Easiest: annotate your implementation with `@Component`
- Or: declare a `@Bean` in an `@AutoConfiguration` or `@Configuration` class

Example
```java
@Component
public class MyAuditPlugin implements LoggedPlugin {
  public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
    if (!options.onCall()) return;
    // emit or enrich
  }
  public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) { }
  public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable ex) { }
  public String announceLoad() { return "MyAudit plugin ready"; }
  public void onLoad() { }
  public void afterMethod() { }
}
```

Best practices
- Be fast: avoid heavy reflection or JSON; prefer single-string messages and reuse `Data` values.
- Don’t throw from hooks; log and move on to avoid breaking the caller.
- Clean up thread-local state in `afterMethod()`.

---

## `Data` — Per-invocation Model

Holds tokens, arguments, timing and depth for the current call. Plugins can read and add tokens.

Key methods
- `tokens()` → `Map<LogToken,String>`: enum-keyed tokens
- `tok()` → `Map<String,String>`: string-keyed tokens using short keys (for template interpolation)
- `args()` → `Arg[]`: argument list (type/name/value already masked/truncated by engine rules)
- `addToken(LogToken, String)`: put/override a known token
- `addFlexToken(String, String)`: add a custom string-keyed token
- `get(LogToken)` / `get(String)`: read token value
- `depth()` → nesting depth (0-based)
- `start()` → millis at call start (used to compute duration)

Notes
- Values in `Data` already respect masking and `maxValueLength`.
- `argNames()` returns a `List<String>` with parameter names.

---

## `Arg` — Argument View

Simple record: `className`, `name`, `value`. Includes `toString(template, valuesMode, truncate)` to render one argument using a mini-token set:
- `{c}` → argument type (simple name)
- `{k}` → argument name
- `{v}` → argument value (masked/truncated depending on `valuesMode`)

Example
```java
Arg a = new Arg("String", "user", "alice");
String s = a.toString("({c}){k}:{v}", Logged.Values.ALL, 64);
// => (String)user:alice
```

---

## `LogToken` — Tokens and Short Keys

Well-known tokens the engine and plugins use. Each enum has a short key for templates (in quotes).

- METHOD_NAME (`m`) — method name
- METHOD_TYPE (`t`) — declared return type (simple name)
- CLASS_NAME (`c`) — simple class name
- CLASS_LONG (`C`) — fully-qualified class name
- ARGUMENTS (`a`) — printable arguments list
- RETURN_VALUE (`rV`) — return value (string)
- RETURN_CLASS (`rC`) — runtime return type (simple)
- DURATION (`d`) — duration in ms
- EXCEPTION_CLASS (`ex`) — exception class (simple)
- EXCEPTION_MESSAGE (`eM`) — exception message
- EXCEPTION_ORIGIN_CLASS (`ec`) — origin class for exception
- EXCEPTION_ORIGIN_METHOD (`em`) — origin method for exception
- LINE (`L`) — line number
- FILENAME (`f`) — source file
- CALL_ICON (`cI`) — call icon
- RETURN_ICON (`rI`) — return icon
- EXCEPTION_ICON (`exI`) — exception icon
- DEPTH_ICON (`dI`) — depth icon
- DEPTH (`h`) — visual depth string
- NULL (`null`) — literal null marker
- MDC (`mdc.`) — prefix used by the SLF4J plugin to expose MDC entries as `{mdc.<key>}`

Tip: use `data.tok()` with `StringInterpolator` to resolve templates that reference these short keys.

---

## `LogDecorator` — Formatting Adapter

Adapter for aesthetic helpers (colors, banners, masking, centering). A `LogDecorator` bean is injected into plugins.

Common methods
- Colors: `green/red/yellow/blue/orange/pink/cyan/magenta/white/lightGray/darkGray`
- Rainbow/gradients: `rainbowify`
- Custom RGB: `custom(r,g,b,s)`, `customBg(r,g,b,s)`, `custom(ColorEnum,s)`
- Masking helpers: `mask(String,int,char)` and `mask(char[],int,char)`
- Banners: `bannerize(color,s,width)`, `bannerize(s,width)`, `ornament(width)`, `center(s,width)`
- Utilities: `fill`, `daySuffix`, `capitalize`, `clearColor`, `reset`

Usage
- Use for human-facing enhancements or colored startup banners from `announceLoad()`.
- Keep runtime log output neutral unless your sinks support ANSI.

---

## Putting It Together — Minimal Custom Plugin

```java
@AutoConfiguration
public class MyPluginConfig {
  @Bean MyPlugin myPlugin(LogDecorator deco) { return new MyPlugin(deco); }
}

final class MyPlugin implements LoggedPlugin {
  private final LogDecorator deco;
  MyPlugin(LogDecorator deco) { this.deco = deco; }

  @Override public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
    if (!options.onCall()) return;
    // Add a custom token and log via slf4j yourself if desired
    data.addFlexToken("env", System.getProperty("env", "dev"));
  }
  @Override public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) { }
  @Override public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable ex) { }
  @Override public void afterMethod() { /* clear thread-locals if needed */ }
  @Override public String announceLoad() { return deco.blue("MyPlugin initialized"); }
  @Override public void onLoad() { }
}
```

Guidelines
- Avoid re-stringifying large objects; prefer values already in `Data`.
- If you modify tokens with `addToken`, use consistent keys (`LogToken`) so other plugins benefit.
- Keep hooks non-blocking; defer heavy work off-thread if you must (but plugins are intended to be lightweight).

---

## Compatibility & Versioning

- The API in `io.github.darkona.logged.api` is stable within a minor series. Breaking changes are called out in release notes.
- Plugins are ordinary Spring beans; you can ship them in your app or as separate libraries.

