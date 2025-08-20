package io.github.darkona.logged.utils;

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
 * This class is thread-safe as it maintains no mutable state.
 * </p>
 */
@SuppressWarnings("unused")
public class StringInterpolator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    private static final Pattern DEFAULTABLE_PATTERN = Pattern.compile("\\{([^}:]+)(?::([^}]*))?}");


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
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
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
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            if (!values.containsKey(key)) {
                throw new IllegalArgumentException("Missing value for placeholder: " + key);
            }
            String replacement = values.get(key);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
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
        Matcher matcher = DEFAULTABLE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String defaultValue = matcher.group(2);
            String replacement = values.getOrDefault(key, defaultValue != null ? defaultValue : matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

}
