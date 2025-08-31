package io.github.darkona.logged.utils;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.Locale;

/**
 * Text transformation and formatting utilities used by the logging library.
 * <p>
 * Designed to be CPU/GC-friendly on logging hot paths: avoids unnecessary allocations,
 * uses fast paths for common types, and reserves more expensive operations (e.g., grapheme
 * handling) for specific methods that are not invoked by default.
 * </p>
 */
@SuppressWarnings("unused")
@Component
public class Transformer {

    private Transformer() {}

    /**
     * Returns a safe String representation of the given object.
     * <ul>
     *   <li>Fast paths (no try/catch) for {@link CharSequence}, numeric wrappers, {@link Boolean} and {@link Character}.</li>
     *   <li>Arrays: renders contents via {@code Arrays.toString/deepToString} instead of identity hashes.</li>
     *   <li>Safe fallback: wraps {@code toString()} in try/catch (prevents logging failures if {@code toString} throws).</li>
     *   <li>No truncation here: callers (e.g., LoggedEngine) handle truncation to avoid duplicate cost.</li>
     * </ul>
     *
     * @param o object to stringify
     * @return textual representation; "null" if the object is null
     */
    public static String objectString(Object o) {
        if (o == null) return "null";
        if (o instanceof CharSequence cs) return cs.toString();
        if (o instanceof Number || o instanceof Boolean || o instanceof Character) return String.valueOf(o);
        // Arrays: render contents instead of identity hash
        Class<?> c = o.getClass();
        if (c.isArray()) {

            if (o instanceof Object[] arr) return Arrays.deepToString(arr);

            switch (o) {
                case int[] a -> {
                    return Arrays.toString(a);
                }
                case long[] a -> {
                    return Arrays.toString(a);
                }
                case double[] a -> {
                    return Arrays.toString(a);
                }
                case float[] a -> {
                    return Arrays.toString(a);
                }
                case boolean[] a -> {
                    return Arrays.toString(a);
                }
                case byte[] a -> {
                    return Arrays.toString(a);
                }
                case short[] a -> {
                    return Arrays.toString(a);
                }
                case char[] a -> {
                    return Arrays.toString(a);
                }
                default -> {}
            }
        }
        try {
            return o.toString();
        } catch (Throwable t) {
            return "toString Error: " + c.getSimpleName();
        }
    }

    /**
     * Repeats the given string {@code amount} times.
     *
     * @param s      pattern to repeat (non-null)
     * @param amount number of repetitions (returns empty string if {@code amount <= 0})
     * @return the repeated string
     */
    public static String fill(String s, int amount) {
        return s.repeat(Math.max(0, amount));
    }

    /**
     * Masks a string, optionally preserving the first {@code unmasked} characters.
     * Does not create a {@code char[]} when masking the entire string; uses {@code repeat} to minimize allocations.
     *
     * @param string   input (may be null - returns null)
     * @param unmasked number of leading characters to leave unmasked
     * @param maskChar mask character (null ? '*')
     * @return masked string or null if {@code string} is null
     */
    public static String mask(String string, @Nullable Integer unmasked, @Nullable Character maskChar) {
        if (string == null) return null;
        int keep = (unmasked == null || unmasked < 0) ? 0 : unmasked;
        char m = (maskChar == null) ? '*' : maskChar;
        int len = string.length();
        if (keep <= 0) return String.valueOf(m).repeat(len);
        if (keep >= len) return string;
        StringBuilder sb = new StringBuilder(len);
        sb.append(string, 0, keep);
        sb.append(String.valueOf(m).repeat(len - keep));
        return sb.toString();
    }

    /**
     * Primitive overload for {@link #mask(String, Integer, Character)}.
     */
    public static String mask(String string, int unmasked, char maskChar) {
        return mask(string, Integer.valueOf(unmasked), Character.valueOf(maskChar));
    }

    /**
     * Masks a character array preserving the first {@code unmasked} characters (optional).
     * Pre-allocates capacity and avoids per-character branching where possible.
     *
     * @param bytes    input characters (may be null - returns null)
     * @param unmasked number of leading characters to leave unmasked
     * @param maskChar mask character (null ? '*')
     * @return masked string, or a copy of {@code bytes} if {@code unmasked >= length}
     */
    public static String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character maskChar) {
        if (bytes == null) return null;
        int keep = (unmasked == null || unmasked < 0) ? 0 : unmasked;
        char m = (maskChar == null) ? '*' : maskChar;
        int len = bytes.length;
        if (keep >= len) return new String(bytes);
        StringBuilder sb = new StringBuilder(len);
        if (keep > 0) sb.append(bytes, 0, keep);
        sb.append(String.valueOf(m).repeat(Math.max(0, len - keep)));
        return sb.toString();
    }

    /**
     * Primitive overload for {@link #mask(char[], Integer, Character)}.
     */
    public static String mask(char[] bytes, int unmasked, char maskChar) {
        return mask(bytes, Integer.valueOf(unmasked), Character.valueOf(maskChar));
    }

    /**
     * English ordinal suffix for a day of the month (st, nd, rd, th).
     *
     * @param day day of month
     * @return ordinal suffix string
     */
    public static String daySuffix(int day) {
        if (day >= 11 && day <= 13) return "th";
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    /**
     * Substring between indices {@code begin} (inclusive) and {@code end} (exclusive).
     * Returns an empty string for null/empty input; returns the original string when indices are invalid.
     */
    public static String getSubstring(String str, int begin, int end) {
        if (str == null || str.isEmpty()) return "";
        return (end <= str.length() && begin < end && begin >= 0) ? str.substring(begin, end) : str;
    }

    /**
     * Substring from {@code begin} to the first occurrence of {@code delimiter} (exclusive).
     * If the delimiter is not present, returns {@code str.trim()}.
     */
    public static String getSubstringUntil(String str, int begin, String delimiter) {
        if (str == null || str.isEmpty()) return "";
        int from = Math.max(0, begin);
        int idx = str.indexOf(delimiter, from);
        if (idx == -1) return str.trim();
        return (from < idx) ? str.substring(from, idx) : "";
    }

    /**
     * Capitalizes the first character (ASCII; not locale-aware).
     * Keeps the rest of the string without unnecessary copies.
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        char first = Character.toUpperCase(s.charAt(0));
        if (s.length() == 1) return String.valueOf(first);
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(first).append(s, 1, s.length());
        return sb.toString();
    }

    /**
     * Ellipsis character used by {@link #truncate(String, int)}.
     */
    private static final String ELLIPSIS = "\u2026";

    /**
     * Truncates the string to at most {@code max} characters and appends {@link #ELLIPSIS}
     * when truncated (so the resulting length is > {@code max}).
     *
     * @param s   input (may be null - returns null)
     * @param max maximum length before the suffix
     * @return truncated string with suffix or the original if no truncation is required
     */
    public static String truncate(String s, int max) {
        if (s == null) return null;
        if (max <= 0) return "";
        if (s.length() <= max) return s;
        // Keep up to max chars, then append ellipsis so total length > max
        return s.substring(0, max) + ELLIPSIS;
    }

    /**
     * Truncates by grapheme clusters (user-perceived characters) using {@link BreakIterator}.
     * Expensive; use only when you must avoid splitting emojis/combining marks.
     *
     * @param s           input (null ? empty string)
     * @param maxClusters maximum number of graphemes
     * @return substring limited to {@code maxClusters} graphemes
     * @throws IllegalArgumentException if {@code maxClusters} less than 0
     */
    public static String truncateGraphemes(String s, int maxClusters) {
        if (s == null) return "";
        if (maxClusters < 0) throw new IllegalArgumentException("maxClusters cannot be a negative number");
        if (s.length() <= maxClusters) return s;
        BreakIterator bi = BreakIterator.getCharacterInstance(Locale.ROOT);
        bi.setText(s);
        int end = bi.first();
        for (int i = 0; i < maxClusters; i++) {
            int next = bi.next();
            if (next == BreakIterator.DONE) return s;
            end = next;
        }
        return s.substring(0, end);
    }
}