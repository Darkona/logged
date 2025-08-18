package io.github.darkona.logged.strings;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for creating styled console banners, ornaments, and centered text output.
 * <p>
 * The {@code Bannerizer} provides helper methods to:
 * <ul>
 *   <li>Create visually styled banners using box-drawing or ASCII characters</li>
 *   <li>Center strings, including those containing ANSI formatting codes</li>
 *   <li>Generate consistent ornaments and dividers</li>
 *
 *   <li>Strip ANSI color codes from strings</li>
 * </ul>
 * These methods are intended for use in CLI applications, log outputs, or development tools where readable,
 * decorated output improves UX or traceability.
 * </p>
 *
 * <p>Example usages:</p>
 * <pre>{@code
 * Bannerizer.bannerize("System Initialized", 40);
 * Bannerizer.center("Version 1.0", 30);
 * Bannerizer.ornament(20);
 * }</pre>
 *
 * <p>This class is stateless and all methods are static.</p>
 */
public class Bannerizer {

    private static final String u_d_top_l = "╔";
    private static final String u_d_top_r = "╗";
    private static final String u_d_bot_l = "╚";
    private static final String u_d_bot_r = "╝";
    private static final String u_d_hor = "═";
    private static final String u_d_ver = "║";
    private static final String u_d_top_t = "╦";
    private static final String u_d_bot_t = "╩";
    private static final String u_d_left_t = "╠";
    private static final String u_d_right_t = "╣";
    private static final String u_d_cross = "╬";

    private static final String d_hor = "-";
    private static final String d_ver = "|";
    private static final String d_cross = "+";

    private Bannerizer() {}

    /**
     * Centers a string within the given width, accounting for visible character length.
     * <p>
     * This method strips formatting codes (e.g., ANSI colors) from the input to determine its visual length,
     * so the centering appears correct when printed to a terminal. The original formatted string is preserved
     * in the output.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * center("Hello", 11) → "   Hello   "
     * </pre>
     *
     * @param s     the string to center (may include formatting codes; may be {@code null})
     * @param width the total width to center the string in
     * @return the centered string, or an empty string if {@code s} is {@code null}
     */
    public static String center(String s, int width) {
        if (s == null) return "";
        var msg = clearColor(s);
        return specialCenter(s, msg, width);
    }

    /**
     * Creates a decorative ornament line of the specified width, framed with double pipe symbols.
     * <p>
     * The resulting string has the form: {@code "||====...====||"}, where the number of {@code '='} characters
     * is equal to the specified width.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * ornament(20) → "||====================||"
     * </pre>
     *
     * @param width the number of {@code '='} characters to include between the framing pipes
     * @return the formatted ornament string
     */
    public static String ornament(int width) {
        return "||" + Transformer.fill("=", width) + "||";
    }

    /**
     * Bannerize a string. Will make the string look like this if no UTF-8 is available:
     * <pre>
     *     +----------------◄►----------------+
     *     |              String              |
     *     +----------------◄►----------------+
     * </pre>
     * If UTF-8 is available, it will look like this:
     * <pre>
     *     ╔═════════════════◄►═══════════════╗
     *     ║              String              ║
     *     ╚═════════════════◄►═══════════════╝
     *  </pre>
     *
     * @param color the color to use to paint the banner. Use ansi color codes.
     * @param s     the string to bannerize
     * @param width the width of the banner
     * @return the string inside a banner
     */
    public static String bannerize(String color, String s, int width) {
        return System.out.charset().equals(StandardCharsets.UTF_8) ?
               bannerize(color, s, width, u_d_ver, u_d_hor, u_d_top_l, u_d_top_r, u_d_bot_l, u_d_bot_r, "◄", "►") :
               bannerize(color, s, width, d_ver, d_hor, d_cross, d_cross, d_cross, d_cross, "◄", "►");
    }

    /**
     * Formats a string as a centered banner block using either ASCII or UTF-8 box-drawing characters.
     * <p>
     * If UTF-8 characters are supported, the banner will use box-drawing characters for borders.
     * Otherwise, it falls back to a plain ASCII-style layout.
     * </p>
     *
     * <p>Example output (ASCII fallback):</p>
     * <pre>
     * +----------------◄►----------------+
     * |              Hello               |
     * +----------------◄►----------------+
     * </pre>
     *
     * <p>Example output (UTF-8 mode):</p>
     * <pre>
     * ╔════════════════◄►════════════════╗
     * ║              Hello               ║
     * ╚════════════════◄►════════════════╝
     * </pre>
     *
     * @param s     the string to bannerize
     * @param width the total width of the banner, including padding and borders
     * @return the formatted banner string
     */
    public static String bannerize(String s, int width) {
        return bannerize("", s, width);
    }

    /**
     * Creates a fully customized banner around the given string, allowing full control over layout characters, colors,
     * and framing symbols. This method supports multi-line strings and adjusts centering based on visible characters
     * (excluding ANSI or other formatting codes).
     * <p>
     * If the string's visible content is too wide for the given banner width, the original string is returned unmodified.
     * </p>
     *
     * <p>Example (UTF-8 styled):</p>
     * <pre>
     * ╔════════════◄►════════════╗
     * ║        Hello World       ║
     * ╚════════════◄►════════════╝
     * </pre>
     *
     * @param color      an optional ANSI color prefix to apply (can be empty if no color is desired)
     * @param s          the string to bannerize (may include ANSI or formatting codes)
     * @param width      total width of the banner (must be greater than string length + 4)
     * @param vertical   the character to use for vertical borders (e.g. {@code │} or {@code |})
     * @param horizontal the character to use for horizontal lines (e.g. {@code ─} or {@code -})
     * @param topL       top-left corner character (e.g. {@code ╔} or {@code +})
     * @param topR       top-right corner character
     * @param botL       bottom-left corner character
     * @param botR       bottom-right corner character
     * @param centerL    character to display before the center marker (e.g. {@code ◄})
     * @param centerR    character to display after the center marker (e.g. {@code ►})
     * @return the full banner string, with borders and optional color codes, or the original string if it exceeds width
     */
    public static String bannerize(String color, String s, int width, String vertical, String horizontal, String topL, String topR, String botL, String botR, String centerL, String centerR) {
        StringBuilder sb = new StringBuilder();
        if (s == null) return "";
        var clean = clearColor(s);
        if (clean.length() > width - 4) return s;

        var top = topL + Transformer.fill(horizontal, width / 2 - 2) + centerL + centerR + Transformer.fill(horizontal, width / 2 - 2) + topR;
        var bottom = botL + Transformer.fill(horizontal, width / 2 - 2) + centerL + centerR + Transformer.fill(horizontal, width / 2 - 2) + botR;

        if (!color.isEmpty()) sb.append(color);

        sb.append(top).append(System.lineSeparator());

        for (var line : s.split(System.lineSeparator())) {
            sb.append(vertical).append(" ");
            sb.append(specialCenter(line, clean, width - 2));
            if (!color.isEmpty()) sb.append(color);
            sb.append(vertical).append(" ");
            sb.append(System.lineSeparator());
        }

        sb.append(bottom);
        if (!color.isEmpty()) sb.append(Colorizer.reset());
        return sb.toString();
    }

    /**
     * Centers a formatted string within a given width, using a "clean" version (without formatting codes)
     * to accurately calculate visible length and padding.
     * <p>
     * This method is useful for aligning colored or decorated text in console output, where the visible length
     * differs from the raw string due to ANSI escape codes or other formatting.
     * </p>
     *
     * @param s     the original string (may contain formatting codes)
     * @param clean the "clean" version of {@code s}, with formatting codes removed
     * @param width the total width to center within
     * @return the centered string with proper padding; or an empty string if {@code s} is {@code null}
     */
    public static String specialCenter(String s, String clean, int width) {
        if (s == null) return "";
        if (width <= clean.length()) return s;
        var half = (width - clean.length()) / 2;
        var space = (clean.length() % 2 == 0) ? (0 != width % 2) ? half + 1 : half - 1 : half;
        return Transformer.fill(" ", half) + s + Transformer.fill(" ", space);
    }


    /**
     * Removes ANSI color codes (escape sequences) from the given string.
     * <p>
     * This is useful for cleaning up log output or terminal strings that include color formatting,
     * such as {@code \u001B[31m} for red or {@code \u001B[0m} to reset formatting.
     * </p>
     *
     * @param s the string potentially containing ANSI color codes
     * @return the cleaned string with all ANSI color codes removed
     */
    public static String clearColor(String s) {
        return s.replaceAll("\u001B\\[[\\d;]*[m;]", "");
    }
}
