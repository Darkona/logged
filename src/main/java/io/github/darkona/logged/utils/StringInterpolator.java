package io.github.darkona.logged.utils;

import io.github.darkona.logged.internals.BoundedLruMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * <p>
 * Callers with a stable template (e.g. one bound from configuration at startup) can skip the
 * cache entirely: {@link #compile(String)} / {@link #compileWithDefaults(String)} return a
 * reusable {@link Template} whose {@link Template#render(Map)} applies values without
 * reparsing or cache lookups.
 *
 * Escaping:
 * - "{{" renders a single '{'
 * - "}}" renders a single '}'
 *
 * Defaults syntax:
 * - Use {key:default} to provide a fallback when the key is absent (only in
 *   {@link #interpolateWithDefaults(String, Map)} and {@link #compileWithDefaults(String)}).
 *   The first ':' splits key/default; the default may contain additional ':' characters.
 * </p>
 */
@SuppressWarnings("unused")
public class StringInterpolator {

    // --- Micro-cache for compiled templates ---
    private static final int CACHE_CAPACITY = 256;
    private static final Map<String, CompiledTemplate> CACHE_PLAIN = Collections.synchronizedMap(new BoundedLruMap<>(CACHE_CAPACITY));
    private static final Map<String, CompiledTemplate> CACHE_DEFAULTABLE = Collections.synchronizedMap(new BoundedLruMap<>(CACHE_CAPACITY));

    private enum Mode { PLAIN, DEFAULTABLE }

    private static final class CompiledTemplate {
        final List<Segment> segments;
        CompiledTemplate(List<Segment> segments) { this.segments = segments; }
    }

    /**
     * A reusable pre-parsed template. Obtain via {@link #compile(String)} or
     * {@link #compileWithDefaults(String)}; call {@link #render(Map)} on each event.
     * Immutable and safe for concurrent use.
     */
    public static final class Template {
        private final CompiledTemplate ct;
        private final int sizeHint;

        private Template(CompiledTemplate ct, int sizeHint) {
            this.ct = ct;
            this.sizeHint = sizeHint;
        }

        /** True when this template was compiled from a null or empty string and always renders "". */
        public boolean isEmpty() {
            return ct.segments.isEmpty();
        }

        /** Applies the given values to this template. Missing keys behave as in the non-strict methods. */
        public String render(Map<String, String> values) {
            if (ct.segments.isEmpty()) return "";
            if (values == null) values = Map.of();
            StringBuilder out = new StringBuilder(sizeHint);
            for (Segment s : ct.segments) s.render(out, values, false);
            return out.toString();
        }
    }

    private static final Template EMPTY_TEMPLATE = new Template(new CompiledTemplate(List.of()), 0);

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

    /**
     * Pre-parses a template with plain {@code {key}} placeholders, bypassing the LRU cache.
     * Ideal for templates that are stable for the lifetime of the application (configuration
     * values): parse once at startup, then {@link Template#render(Map)} per event.
     */
    public static Template compile(String template) {
        if (template == null || template.isEmpty()) return EMPTY_TEMPLATE;
        return new Template(parse(template, Mode.PLAIN), template.length() + 16);
    }

    /**
     * Pre-parses a template supporting {@code {key:default}} placeholders, bypassing the LRU cache.
     * See {@link #compile(String)}.
     */
    public static Template compileWithDefaults(String template) {
        if (template == null || template.isEmpty()) return EMPTY_TEMPLATE;
        return new Template(parse(template, Mode.DEFAULTABLE), template.length() + 16);
    }

    private static CompiledTemplate compileCached(String template, Mode mode) {
        Map<String, CompiledTemplate> cache = (mode == Mode.DEFAULTABLE) ? CACHE_DEFAULTABLE : CACHE_PLAIN;
        CompiledTemplate cached = cache.get(template);
        if (cached != null) return cached;
        CompiledTemplate ct = parse(template, mode);
        cache.put(template, ct);
        return ct;
    }

    private static CompiledTemplate parse(String template, Mode mode) {
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
        return new CompiledTemplate(List.copyOf(segs));
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
        if (template == null || template.isEmpty()) return "";
        if (values == null) values = Map.of();
        CompiledTemplate ct = compileCached(template, Mode.PLAIN);
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
        if (template == null || template.isEmpty()) return "";
        if (values == null) values = Map.of();
        CompiledTemplate ct = compileCached(template, Mode.PLAIN);
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
        if (template == null || template.isEmpty()) return "";
        if (values == null) values = Map.of();
        CompiledTemplate ct = compileCached(template, Mode.DEFAULTABLE);
        StringBuilder out = new StringBuilder(template.length() + 16);
        for (Segment s : ct.segments) s.render(out, values, false);
        return out.toString();
    }

}
