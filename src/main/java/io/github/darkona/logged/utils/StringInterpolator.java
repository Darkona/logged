package io.github.darkona.logged.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for interpolating placeholders within a string template using values from a provided map.
 * <p>
 * Placeholders are denoted using curly braces, e.g. {@code {name}}, and will be replaced with corresponding
 * values from the input map. If a placeholder key is not found, the placeholder is left unchanged.
 * </p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * Map<String, String> values = Map.of("name", "Javi", "lang", "Java");
 * String result = StringInterpolator.interpolate("Hello {name}, welcome to {lang}!", values);
 * // result: "Hello Javi, welcome to Java!"
 * }</pre>
 *
 * <p>
 * Thread-safety: uses small synchronized LRU caches (256 entries per mode) to store a compiled
 * representation of recently used templates. Methods are safe for concurrent use without
 * external synchronization.
 *
 * Escaping:
 * - "{{" renders a single '{'
 * - "}}" renders a single '}'
 *
 * Defaults syntax:
 * - Use {key:default} to provide a fallback when the key is absent (only in
 *   {@link #interpolateWithDefaults(String, Map)}). The first ':' splits key/default; the default
 *   may contain additional ':' characters.
 * </p>
 */
@SuppressWarnings("unused")
public class StringInterpolator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    private static final Pattern DEFAULTABLE_PATTERN = Pattern.compile("\\{([^}:]+)(?::([^}]*))?}");

    // --- Micro-cache for compiled templates ---
    private static final int CACHE_CAPACITY = 256;
    private static final Map<String, CompiledTemplate> CACHE_PLAIN = java.util.Collections.synchronizedMap(new LruMap(CACHE_CAPACITY));
    private static final Map<String, CompiledTemplate> CACHE_DEFAULTABLE = java.util.Collections.synchronizedMap(new LruMap(CACHE_CAPACITY));

    private enum Mode { PLAIN, DEFAULTABLE }

    private static final class LruMap extends LinkedHashMap<String, CompiledTemplate> {
        private final int capacity;
        LruMap(int capacity) { super(capacity, 0.75f, true); this.capacity = capacity; }
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CompiledTemplate> eldest) {
            return size() > capacity;
        }
    }

    private static final class CompiledTemplate {
        final List<Segment> segments;
        CompiledTemplate(List<Segment> segments) { this.segments = segments; }
    }

    private interface Segment { void render(StringBuilder out, Map<String,String> values, boolean strict); }

    private static final class Literal implements Segment {
        final String text;
        Literal(String text) { this.text = text; }
        public void render(StringBuilder out, Map<String,String> values, boolean strict) { out.append(text); }
    }

    private static final class Placeholder implements Segment {
        final String key;           // token key
        final String def;           // default (only for DEFAULTABLE mode)
        final String rawInside;     // original inside for fallback display
        Placeholder(String key, String def, String rawInside) { this.key = key; this.def = def; this.rawInside = rawInside; }
        public void render(StringBuilder out, Map<String,String> values, boolean strict) {
            if (values.containsKey(key)) {
                out.append(values.get(key));
            } else if (def != null) {
                out.append(def);
            } else if (strict) {
                throw new IllegalArgumentException("Missing value for placeholder: " + key);
            } else {
                out.append('{').append(rawInside).append('}');
            }
        }
    }

    private static CompiledTemplate compile(String template, Mode mode) {
        if (template == null || template.isEmpty()) return new CompiledTemplate(List.of(new Literal(template)));
        // Select cache
        Map<String, CompiledTemplate> cache = (mode == Mode.DEFAULTABLE) ? CACHE_DEFAULTABLE : CACHE_PLAIN;
        CompiledTemplate cached = cache.get(template);
        if (cached != null) return cached;

        List<Segment> segs = new ArrayList<>();
        int i = 0, n = template.length();
        StringBuilder lit = new StringBuilder();
        while (i < n) {
            char ch = template.charAt(i);
            if (ch == '{') {
                // Handle escaped '{{' -> '{'
                if (i + 1 < n && template.charAt(i + 1) == '{') {
                    lit.append('{');
                    i += 2;
                    continue;
                }
                // Flush literal collected so far
                if (lit.length() > 0) { segs.add(new Literal(lit.toString())); lit.setLength(0); }
                int close = template.indexOf('}', i + 1);
                if (close < 0) { // no closing brace, treat rest as literal
                    lit.append(template, i, n);
                    break;
                }
                String inside = template.substring(i + 1, close);
                if (mode == Mode.DEFAULTABLE) {
                    int colon = inside.indexOf(':');
                    String key = (colon >= 0) ? inside.substring(0, colon) : inside;
                    String def = (colon >= 0) ? inside.substring(colon + 1) : null;
                    segs.add(new Placeholder(key, def, inside));
                } else {
                    segs.add(new Placeholder(inside, null, inside));
                }
                i = close + 1;
            } else if (ch == '}') {
                // Handle escaped '}}' -> '}'
                if (i + 1 < n && template.charAt(i + 1) == '}') {
                    lit.append('}');
                    i += 2;
                } else {
                    lit.append('}');
                    i += 1;
                }
            } else {
                lit.append(ch);
                i += 1;
            }
        }
        if (lit.length() > 0) segs.add(new Literal(lit.toString()));
        CompiledTemplate ct = new CompiledTemplate(List.copyOf(segs));
        cache.put(template, ct);
        return ct;
    }

    /**
     * Replaces placeholders in the given template string with values from the provided map.
     * <p>
     * Placeholders must be enclosed in curly braces (e.g., {@code {username}}). If a key is not found
     * in the map, the placeholder will be left as-is in the resulting string.
     * </p>
     *
     * @param template the input string containing placeholders
     * @param values   the map of placeholder keys to replacement values
     * @return the interpolated string with placeholders replaced
     */
    public static String interpolate(String template, Map<String, String> values) {
        CompiledTemplate ct = compile(template, Mode.PLAIN);
        StringBuilder out = new StringBuilder(template.length() + 16);
        for (Segment s : ct.segments) s.render(out, values, false);
        return out.toString();
    }

    /**
     * Replaces placeholders in the given template string with values from the provided map.
     * <p>
     * If a placeholder key is missing from the map, this method will throw an {@link IllegalArgumentException}.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * interpolateStrict("Hello {name}", Map.of("name", "Javi")) → "Hello Javi"
     * interpolateStrict("Hello {name}, you use {lang}", Map.of("name", "Javi")) → ❌ throws
     * </pre>
     *
     * @param template the input string containing placeholders
     * @param values   the map of placeholder keys to replacement values
     * @return the interpolated string
     * @throws IllegalArgumentException if a placeholder is not found in the map
     */
    public static String interpolateStrict(String template, Map<String, String> values) {
        CompiledTemplate ct = compile(template, Mode.PLAIN);
        StringBuilder out = new StringBuilder(template.length() + 16);
        for (Segment s : ct.segments) s.render(out, values, true);
        return out.toString();
    }


    /**
     * Replaces placeholders in the template with values from the map, supporting default values using {@code {key:default}} syntax.
     * <p>
     * If a key is not found in the map, the provided default value (after the colon) will be used.
     * If no default is present, the placeholder is left unchanged.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * interpolateWithDefaults("Hello {name:guest}", Map.of()) → "Hello guest"
     * interpolateWithDefaults("Lang: {lang:Java}", Map.of("lang", "Rust")) → "Lang: Rust"
     * </pre>
     *
     * @param template the input string with optional-default placeholders
     * @param values   the map of placeholder keys to replacement values
     * @return the interpolated string
     */
    public static String interpolateWithDefaults(String template, Map<String, String> values) {
        CompiledTemplate ct = compile(template, Mode.DEFAULTABLE);
        StringBuilder out = new StringBuilder(template.length() + 16);
        for (Segment s : ct.segments) s.render(out, values, false);
        return out.toString();
    }

}
