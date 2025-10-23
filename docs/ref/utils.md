# Utilities — Extended Documentation

Logged ships a small set of lightweight utilities used by the engine and available to consumers. This page documents each utility in depth, with behavior, usage, and performance notes.

- Source folder: `src/main/java/io/github/darkona/logged/utils/`
  - `Transformer.java`
  - `StringInterpolator.java`
  - `Colorizer.java`
  - `Bannerizer.java`
  - `SymbolTheme.java`

---

## Transformer

Purpose
- Fast, allocation-aware helpers for stringifying values, masking, truncation, and simple text operations used in hot paths.

Key APIs
- `objectString(Object)`: safe string form of any object.
  - Fast paths: `CharSequence`, numbers, `Boolean`, `Character`.
  - Arrays: uses `Arrays.toString/deepToString` to avoid identity hashes.
  - Fallback: `toString()` under try/catch; on failure returns `"toString Error: <SimpleClass>"`.
- `fill(String s, int amount)`: repeats `s` `amount` times (uses `String#repeat`).
- `mask(String s, Integer keep, Character maskChar)`: masks a string preserving the first `keep` chars; default mask `*`.
  - Overloads for primitives and for `char[]` to avoid boxing.
- `truncate(String s, int max)`: keeps up to `max` chars, then appends an ellipsis so result length is > `max` when truncated.
- `truncateGraphemes(String s, int maxClusters)`: grapheme-safe truncation using `BreakIterator` (slower; use only if needed).
- Small helpers: `daySuffix(int)`, substring variants, ASCII `capitalize`.

Examples
```java
Transformer.objectString(List.of(1,2));     // "[1, 2]"
Transformer.fill("-", 5);                   // "-----"
Transformer.mask("secret-token", 2, '*');   // "se**********"
Transformer.truncate("abcdef", 3);          // "abc…" (ellipsis appended)
```

Performance notes
- `objectString` avoids exceptions on common types and protects against bad `toString()` implementations.
- `truncateGraphemes` is expensive; prefer `truncate` unless you must avoid breaking emojis/combining marks.

---

## StringInterpolator

Purpose
- Resolve `{key}` placeholders from a `Map<String,String>` with optional default values and a strict mode.

Modes & syntax
- Plain: `interpolate(template, values)` — leaves unknown `{key}` unchanged.
- Strict: `interpolateStrict(template, values)` — throws if any key is missing.
- With defaults: `interpolateWithDefaults(template, values)` — supports `{key:default}`.
- Escaping: `{{` renders `{`, `}}` renders `}`.

Examples
```java
var vals = Map.of("name","Darkona","lang","Java");
StringInterpolator.interpolate("Hello {name}, using {lang}", vals);
// => "Hello Darkona, using Java"

StringInterpolator.interpolate("Missing {x}", Map.of());
// => "Missing {x}"   (left as-is)

StringInterpolator.interpolateWithDefaults("Hi {name:guest}", Map.of());
// => "Hi guest"

StringInterpolator.interpolateStrict("Hi {name}", Map.of());
// throws IllegalArgumentException
```

Performance notes
- Uses a tiny synchronized LRU cache (256 entries per mode) for compiled templates; safe for concurrent use.
- Placeholders are parsed once per unique template string, then rendered with simple segment iteration.

---

## Colorizer

Purpose
- ANSI color helpers for terminal/log output; static, stateless.

Key APIs
- Simple colors: `red/green/blue/yellow/orange/pink/aqua/purple/white/gray/darkGray/magenta(String)`.
- Reset: `reset()`.
- Gradients: `rainbowify(String)`, `colorizeChars(String, List<ColorEnum>)`, `colorizeLines(List<String>, List<ColorEnum>)`.
- Custom colors: `custom(Color awt, String)`, `custom(int r, int g, int b, String)`, `custom(ColorEnum, String)`.
- Cleanup: `clearColor(String)` strips ANSI escape codes.

Examples
```java
Colorizer.red("ERROR");
Colorizer.custom(255, 54, 116, "Hot Pink");
Colorizer.rainbowify("Hello, world\nAgain");
Colorizer.clearColor("\u001B[31mRed\u001B[0m"); // => "Red"
```

Notes
- Designed for readability; avoid in logs where raw ANSI is undesired (strip with `clearColor`).
- Color constants live under `io.github.darkona.logged.colors`.

---

## Bannerizer

Purpose
- Build banners, centered lines, simple tables and ornaments — ASCII/UTF skins, ANSI‑aware centering, low overhead.

Key APIs
- Centering: `center(String, int width)` — centers text by visible length (ANSI‑aware).
- Ornaments: `ornament(int width)` — `||====||` style separators.
- Banners (borders + centered lines):
  - `bannerize(String color, String s, int width)` — auto skin (UTF if `System.out` is UTF‑8), optional color prefix.
  - `bannerize(ColorEnum bannerColor, ColorEnum textColor, String s, int width)` — colored border + text.
  - `bannerize(String s, int width)` — border only, no color.
  - `bannerize(String s, int width, boolean withBorder)` — center lines without border when `withBorder=false`.
- Tables: `mapTablerize(...)` — two‑column key/value table with optional colors.
- UTF detection override: `overrideUtf8(Boolean)` to force ASCII/UTF skin.

Examples
```java
Bannerizer.bannerize("Welcome", 40);
Bannerizer.bannerize(ColorEnum.ORANGE, ColorEnum.WHITE, "Status OK", 50);
Bannerizer.center(Colorizer.yellow("Hello"), 30);
var table = Map.of("version","1.4.0","backend","Logback");
Bannerizer.mapTablerize(ColorEnum.BLUE, null, null, null, new String[]{"Key","Value"}, table, 48);
```

Notes
- ANSI‑aware centering: strips color codes when calculating padding, preserves them in output.
- If any line exceeds inner width, returns original text (no banner), to avoid breaking content.

---

## SymbolTheme

Purpose
- Predefined icon themes for call/return/exception/depth symbols. Used when `logged.useIconTheme=true`.

Usage
- Choose a theme under `SymbolTheme` and set `logged.iconTheme` (with `useIconTheme=true`).
- The engine will use `theme.entry() / theme.exit() / theme.exception() / theme.depth()` to populate the icon tokens.

Notes
- Themes include ASCII‑only presets (`SJET_*`, `JUNO_*`) and Unicode/emoji styles. Rendering depends on your console font/encoding.
- You can still override icons directly with `logged.callIcon/returnIcon/exceptionIcon/depthIcon` when not using a theme.

---

## Integration With Logged

- The engine uses `Transformer` to stringify values, enforce `maxValueLength`, and apply masking.
- SLF4J/OpenTelemetry plugins use `StringInterpolator` to resolve template tokens into messages/span names.
- The color log decorator relies on `Colorizer` (and `io.github.darkona.logged.colors.*`).
- Banners are handy for startup messages or human‑friendly logs in tests and CLIs (`Bannerizer`).
- Icon handling can be theme‑based via `SymbolTheme` or string‑based via `logged.*Icon` properties.

---

## Best Practices

- Prefer `Transformer.objectString` for robust, non‑throwing toString in logs.
- Mask first, then truncate (the engine does this); keep `maxValueLength` conservative for performance.
- Use `interpolateStrict` for templates you control and want to validate at startup.
- Avoid heavy colorization in production logs unless your sinks/renderers support ANSI (or strip with `clearColor`).
- When building UIs/CLIs, `Bannerizer` helps create readable boundaries and quick key/value tables.

