# Colors Package — Reference

The `io.github.darkona.logged.colors` package provides color primitives and named palettes used by Logged’s decorators and plugins. It abstracts ANSI escape sequences and offers both simple ANSI colors and 24‑bit RGB (truecolor).

- Package: `src/main/java/io/github/darkona/logged/colors/`
  - `ColorEnum` — common contract for all colors
  - `LogColor` — 24‑bit RGB palette (truecolor)
  - `BasicColor` — basic ANSI color palette (8/16‑color codes)
  - Named palettes: `Blue`, `Red`, `Green`, `Yellow`, `Orange`, `Pink`, `White`, `Gray`, `Brown`
  - `ColorFinder` — resolve a color from a string (name, hex, or ANSI)

---

## ColorEnum

- Minimal interface implemented by all color enums.
- Methods
  - `toString()` → ANSI escape sequence for the foreground color.
  - `red()/green()/blue()` → component accessors (0–255). For `BasicColor` they return 0 (non‑RGB).
  - Static: `reset()` → ANSI reset sequence `"\u001B[0m"`.
- Helper: `assemble(r,g,b)` builds a truecolor foreground sequence `"\u001B[38;2;<r>;<g>;<b>m"`.

Usage
```java
String c = LogColor.GOLD.toString();     // start gold
String reset = ColorEnum.reset();        // reset attributes
```

---

## LogColor (Truecolor RGB)

- Enum of common colors with 24‑bit RGB values (e.g., `RED(255,0,0)`, `GOLD(255,215,0)`).
- `toString()` returns a 24‑bit foreground ANSI code `38;2;r;g;b`.
- Prefer `LogColor` when your terminal/sink supports truecolor.

Example
```java
System.out.println(LogColor.CYAN + "Cyan text" + ColorEnum.reset());
```

---

## BasicColor (ANSI 8/16)

- Enum mapping classic ANSI SGR codes (e.g., `RED(31)`, `BLUE(34)`, `WHITE(97)`).
- `toString()` returns `"\u001B[0;<code>m"`.
- Use when targeting limited terminals or when truecolor is not desired.

Example
```java
System.out.println(BasicColor.GREEN + "OK" + ColorEnum.reset());
```

---

## Named Palettes (Blue/Red/…)

- Enums with curated shades for families like `Blue`, `Red`, `Green`, etc.
- Each constant carries RGB (truecolor) and `toString()` uses `ColorEnum#assemble`.
- Useful for consistent thematic output (e.g., WARN vs OK vs ERROR palettes).

Examples
```java
System.out.println(Blue.DODGER_BLUE + "hello" + ColorEnum.reset());
System.out.println(Red.CRIMSON + "fail" + ColorEnum.reset());
```

Available families
- `Blue`, `Red`, `Green`, `Yellow`, `Orange`, `Pink`, `White`, `Gray`, `Brown`.

---

## ColorFinder

- `ColorFinder.findColor(String)` resolves a `ColorEnum` from flexible inputs:
  - Hex: `#RRGGBB` or `RRGGBB` (e.g., `#ff3674`).
  - Name (case/space/dash insensitive): matches constants in `BasicColor`, then families (`Blue`, `Red`, …).
  - ANSI string (back‑compat): matches an exact `toString()` of known colors.
- Order of resolution (first match wins): `BasicColor` → family enums → ANSI string → default `BasicColor.BLACK`.

Examples
```java
ColorEnum c1 = ColorFinder.findColor("#ff3674");  // hex
ColorEnum c2 = ColorFinder.findColor("dodger-blue"); // Blue.DODGER_BLUE
ColorEnum c3 = ColorFinder.findColor("RED"); // BasicColor.RED
```

---

## Working With Decorators

- `LogDecorator` and `Colorizer` consume `ColorEnum`:
  - Decorator color methods often accept `ColorEnum` or strings resolved via `ColorFinder`.
  - Combine with `ColorEnum.reset()` to ensure styles don’t leak.

Example
```java
String banner = colorDecorator.custom(LogColor.ORANGE.red(), LogColor.ORANGE.green(), LogColor.ORANGE.blue(), "Hi");
```

---

## Tips & Compatibility

- Terminals/sinks:
  - Truecolor (RGB) requires support for `38;2` SGR; most modern terminals support it.
  - Basic ANSI is more widely supported; when in doubt, prefer `BasicColor`.
- Reset after styling with `ColorEnum.reset()` to avoid bleeding styles.
- For log files processed by systems that don’t render ANSI, strip codes (see `Colorizer.clearColor`).
- Colors affect foreground only; background color helpers are exposed by `LogDecorator` (e.g., `customBg`).

