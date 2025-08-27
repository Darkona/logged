package io.github.darkona.logged.colors;

import java.util.List;

public class ColorFinder {

    // Order matters: first match wins (same spirit as your original method)
    private static final List<Class<? extends Enum<?>>> ORDER = List.of(
            BasicColor.class,
            Blue.class,
            Red.class,
            Green.class,
            Yellow.class,
            Orange.class,
            Pink.class,
            White.class,
            Gray.class,
            Brown.class
    );

    private ColorFinder() {}

    public static ColorEnum findColor(String c) {
        if (c == null) return BasicColor.BLACK;

        final String raw = c.trim();
        if (raw.isEmpty()) return BasicColor.BLACK;

        // Normalize: case-insensitive; spaces/hyphens -> underscores
        final String normalized = raw.toUpperCase()
                                     .replace('-', '_')
                                     .replace(' ', '_');

        // 1) Try by enum name (preferred)
        for (Class<? extends Enum<?>> enumClass : ORDER) {
            ColorEnum match = matchByName(enumClass, normalized);
            if (match != null) return match;
        }

        // 2) Back-compat: try by ANSI string (toString())
        for (Class<? extends Enum<?>> enumClass : ORDER) {
            ColorEnum match = matchByAnsi(enumClass, raw);
            if (match != null) return match;
        }

        // 3) Default
        return BasicColor.BLACK;
    }

    @SuppressWarnings("unchecked")
    private static ColorEnum matchByName(Class<? extends Enum<?>> enumClass, String normalized) {
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(normalized)) {
                return (ColorEnum) constant;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static ColorEnum matchByAnsi(Class<? extends Enum<?>> enumClass, String raw) {
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            ColorEnum c = (ColorEnum) constant;
            if (c.toString().equals(raw)) {
                return c;
            }
        }
        return null;
    }
}
