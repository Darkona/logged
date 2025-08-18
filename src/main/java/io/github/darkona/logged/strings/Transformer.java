package io.github.darkona.logged.strings;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


/**
 * Utility class for formatting log messages with optional ANSI color codes
 * and fixed-width padding. Intended for use in development environments
 * to improve readability of structured logs.
 * <p>
 * This class provides helper methods for creating stylized log segments
 * (e.g., tags, labels, values) in colorized or aligned formats.
 * <p>
 * For production environments, usage of this class should be conditional
 * to avoid ANSI escape codes in centralized log systems.
 *
 * @author Darkona
 * @since 1.0
 */
@SuppressWarnings("unused")
@Component
public class Transformer {


    private static final String RESET = "\u001B[0m";
    private static final OutputStreamWriter writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);

    private Transformer() {}


    public static String objectString(Object o) {
        try {
            return o == null ? "null" : o.toString();
        } catch (Throwable t) {
            return "toString Error: " + o.getClass().getSimpleName();
        }
    }


    /**
     * Creates a string by repeating the given input string until the specified amount is reached.
     * <p>
     * If {@code length} is less than or equal to zero, an empty string is returned.
     * This is typically used to generate padding or horizontal separators.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * fill("=", 5) → "====="
     * fill("ab", 3) → "ababab"
     * fill("*", 0) → ""
     * </pre>
     *
     * @param s      the string to repeat (must not be {@code null})
     * @param amount the number of times to repeat the string
     * @return the resulting repeated string, or an empty string if {@code length} ≤ 0
     * @throws NullPointerException if {@code s} is {@code null}
     */
    public static String fill(String s, int amount) {
        return s.repeat(Math.max(0, amount));
    }


    /**
     * Returns a masked version of the given string, preserving a specified number of leading characters.
     * <p>
     * This is a convenience overload of {@link #mask(char[], Integer, Character)} that accepts a {@link String}.
     * Internally, it converts the input to a character array before masking.
     * </p>
     *
     * <p>
     * All characters beyond the {@code unmasked} count will be replaced with the {@code maskChar}.
     * If {@code unmasked} is {@code null} or negative, all characters are masked.
     * If {@code maskChar} is {@code null}, {@code '*'} is used by default.
     * </p>
     *
     * <p>Example: {@code mask("supersecret", 3, '*')} → {@code "sup*******"}</p>
     *
     * @param string   the string to mask (must not be {@code null})
     * @param unmasked the number of leading characters to leave unmasked (may be {@code null})
     * @param maskChar the character to use for masking (may be {@code null})
     * @return the resulting masked string
     * @throws NullPointerException if {@code string} is {@code null}
     */
    public static String mask(String string, @Nullable Integer unmasked, @Nullable Character maskChar) {
        return mask(string.toCharArray(), unmasked, maskChar);
    }

    /**
     * Returns a masked version of the given character array, preserving a specified number of leading characters.
     * <p>
     * All characters beyond the {@code unmasked} count will be replaced with the {@code maskChar}.
     * If {@code unmasked} is {@code null} or negative, all characters are masked.
     * If {@code maskChar} is {@code null}, {@code '*'} is used by default.
     * </p>
     *
     * <p>Example: {@code mask("secret".toCharArray(), 2, '*')} → {@code "se****"}</p>
     *
     * @param bytes    the character array to mask (must not be {@code null})
     * @param unmasked the number of leading characters to leave unmasked (may be {@code null})
     * @param maskChar the character to use for masking (may be {@code null})
     * @return the resulting masked string
     */
    public static String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character maskChar) {
        if (unmasked == null || unmasked < 0) {
            unmasked = 0;
        }
        if (maskChar == null) {
            maskChar = '*';
        }
        var builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            builder.append(i < unmasked ? bytes[i] : maskChar);
        }
        return builder.toString();
    }

    /**
     * Returns the English ordinal suffix for a given day of the month.
     * <p>
     * For example: 1 → "st", 2 → "nd", 3 → "rd", 4 → "th", 11–13 → "th", etc.
     * This method handles the special case for numbers ending in 11–13, which always use "th".
     * </p>
     *
     * @param day the day of the month (1–31)
     * @return the corresponding ordinal suffix: "st", "nd", "rd", or "th"
     */
    public static String daySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    /**
     * Returns a substring from the given string between the specified {@code begin} and {@code end} indexes.
     * <p>
     * If the input string is {@code null} or empty, an empty string is returned.
     * If the specified indexes are invalid (i.e., {@code end} exceeds the string length,
     * {@code begin} is greater than or equal to {@code end}, or {@code begin} is negative),
     * the original string is returned unchanged.
     * </p>
     *
     * @param str   the input string (may be {@code null})
     * @param begin the starting index (inclusive)
     * @param end   the ending index (exclusive)
     * @return the substring from {@code begin} to {@code end} if valid; the original string or an empty string otherwise
     */
    public static String getSubstring(String str, int begin, int end) {
        if (str == null || str.isEmpty()) return "";
        return (end <= str.length() && begin < end && begin >= 0) ? str.substring(begin, end) : str;
    }

    /**
     * Returns a substring of the given string, starting at the specified index and ending at the first occurrence
     * of the given delimiter. It does not include the delimiter.
     * <p>
     * If the input string is {@code null} or empty, an empty string is returned.
     * If the delimiter is not found, the trimmed input string is returned in full.
     * </p>
     *
     * @param str       the original input string (may be {@code null})
     * @param begin     the starting index for the substring (inclusive)
     * @param delimiter the delimiter marking the end of the substring
     * @return the substring from {@code begin} to the first occurrence of {@code delimiter}, or the trimmed input string
     * if the delimiter is not present; never {@code null}
     */
    public static String getSubstringUntil(String str, int begin, String delimiter) {
        if (str == null || str.isEmpty()) return "";
        var trimmed = str.trim();
        var firstSpace = str.indexOf(delimiter);
        if (firstSpace == -1) {
            return trimmed;
        }
        return str.substring(begin, firstSpace);
    }

    /**
     * Capitalizes the first character of the given string.
     * <p>
     * If the string is {@code null} or empty, it is returned as-is.
     * </p>
     *
     * @param s the input string (may be {@code null})
     * @return the string with its first character converted to uppercase; or the original string if null or empty
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}
