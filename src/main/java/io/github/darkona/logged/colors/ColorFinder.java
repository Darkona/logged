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

        // 0) Try hex #RRGGBB (or RRGGBB)
        String hex = raw.startsWith("#") ? raw.substring(1) : raw;
        if (hex.length() == 6 && hex.chars().allMatch(ch -> Character.digit(ch, 16) >= 0)) {
            try {
                short r = (short) Integer.parseInt(hex.substring(0, 2), 16);
                short g = (short) Integer.parseInt(hex.substring(2, 4), 16);
                short b = (short) Integer.parseInt(hex.substring(4, 6), 16);
                return new HexColor(r, g, b);
            } catch (Exception ignored) {
                // fall through
            }
        }

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


    private static ColorEnum matchByName(Class<? extends Enum<?>> enumClass, String normalized) {
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(normalized)) {
                return (ColorEnum) constant;
            }
        }
        return null;
    }

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

/** Lightweight ColorEnum implementation for hex-based colors */
final class HexColor implements ColorEnum {
    private final short r, g, b;
    HexColor(short r, short g, short b) { this.r = r; this.g = g; this.b = b; }
    @Override public String toString() { return assemble(r, g, b); }
    @Override public Short red() { return r; }
    @Override public Short green() { return g; }
    @Override public Short blue() { return b; }
}
