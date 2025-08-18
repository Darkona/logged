package io.github.darkona.logged.strings;

import io.github.darkona.logged.colors.ColorEnum;
import io.github.darkona.logged.colors.LogColor;

import java.awt.*;
import java.util.List;

/**
 * Utility class for applying ANSI color codes to strings for terminal or console output.
 * <p>
 * The {@code Colorizer} class provides a collection of static methods for colorizing strings
 * in specific colors (e.g., red, blue, green), applying custom colors, removing color codes,
 * and dynamically cycling colors across characters or lines.
 * </p>
 *
 * <h3>Key features include:</h3>
 * <ul>
 *   <li>Colorizing text in standard terminal colors</li>
 *   <li>Resetting terminal color state</li>
 *   <li>Applying dynamic color gradients (e.g., rainbow effects)</li>
 *   <li>Stripping ANSI escape codes from formatted strings</li>
 *   <li>Composing custom color sequences for creative output</li>
 * </ul>
 *
 * <h3>Example usages:</h3>
 * <pre>{@code
 * Colorizer.red("ERROR: Something went wrong");
 * Colorizer.colorizeChars("Hello", List.of(LogColor.RED, LogColor.GREEN));
 * Colorizer.clearColor("\u001B[31mRed Text\u001B[0m"); // → "Red Text"
 * }</pre>
 *
 * <p>
 * All methods are static and the class is stateless. Intended for use in CLI tools, logs,
 * banners, or any context where colored output improves readability or style.
 * </p>
 */

public class Colorizer {


    private Colorizer() {}

    /**
     * Returns a color code from a fixed rainbow palette, based on the given index.
     * <p>
     * The rainbow palette includes the following colors in order:
     * <ul>
     *     <li>0 — Red</li>
     *     <li>1 — Orange</li>
     *     <li>2 — Gold</li>
     *     <li>3 — Lime</li>
     *     <li>4 — Blue</li>
     *     <li>5 — Cyan</li>
     *     <li>6 — Magenta</li>
     * </ul>
     * If the index is out of bounds (i.e., not in [0, 6]), an empty string is returned.
     * This is useful for generating colorized output or styling logs with a rainbow gradient.
     * </p>
     *
     * @param i the index of the rainbow color to retrieve (must be between 0 and 6)
     * @return the ANSI color code as a {@link String}, or an empty string if the index is invalid
     */
    public static String rainbowColor(int i) {
        if (i > 6 || i < 0) return "";
        List<ColorEnum> rainbow = List.of(
                LogColor.RED,
                LogColor.ORANGE,
                LogColor.GOLD,
                LogColor.LIME,
                LogColor.BLUE,
                LogColor.CYAN,
                LogColor.MAGENTA
        );
        return rainbow.get(i).toString();
    }

    /**
     * Applies a rainbow color gradient to the input string by coloring each non-whitespace character
     * with a different color from the rainbow palette.
     * <p>
     * The coloring cycles through seven colors (red, orange, gold, lime, blue, cyan, magenta),
     * resetting back to red after each line break. Whitespace characters (including line breaks)
     * are not colorized but are preserved in the output.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * rainbowify("Hello") → [red]H[orange]e[gold]l[lime]l[blue]o[reset]
     * </pre>
     *
     * @param s the input string to rainbowify (may include line breaks)
     * @return the colorized string, ending with a reset code to clear formatting
     */
    public static String rainbowify(String s) {
        //if (!enabled) return s;
        var r = new StringBuilder();
        int x = 0;
        for (int i = 0; i < s.length(); i++) {
            String c = Character.toString(s.charAt(i));
            if (!" ".equals(c) && !System.lineSeparator().equals(c)) {
                r.append(rainbowColor(x));
                x = (x + 1) % 7;
            } else if (System.lineSeparator().equals(c)) {
                x = 0;
            }
            r.append(c);
        }
        r.append(ColorEnum.reset());
        return r.toString();
    }

    /**
     * Applies a sequence of colors to each non-whitespace character in the input string, cycling through
     * the provided list of {@link ColorEnum} values.
     * <p>
     * Color cycling resets on line breaks. Whitespace characters (spaces and newlines) are not colorized,
     * but are preserved in the output. A reset code is appended at the end to restore default terminal color.
     * </p>
     *
     * <p>Example:</p>
     * <pre>
     * colorizeChars("Hello", List.of(RED, GREEN)) → [RED]H[GREEN]e[RED]l[GREEN]l[RED]o[RESET]
     * </pre>
     *
     * @param s      the string to colorize (may include newlines and spaces)
     * @param colors the list of colors to cycle through (must not be null or empty)
     * @return the colorized string, with a reset code at the end
     * @throws IllegalArgumentException if {@code colors} is null or empty
     */
    public static String colorizeChars(String s, List<ColorEnum> colors) {
        if (s == null || s.isEmpty()) return "";
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("Color list must not be null or empty.");
        }

        var b = new StringBuilder();
        int x = 0;

        for (int i = 0; i < s.length(); i++) {
            String c = Character.toString(s.charAt(i));

            if (!" ".equals(c) && !System.lineSeparator().equals(c)) {
                b.append(colors.get(x));
                x = (x + 1) % colors.size();
            } else if (System.lineSeparator().equals(c)) {
                x = 0;
            }

            b.append(c);
        }

        b.append(ColorEnum.reset());
        return b.toString();
    }

    /**
     * Make a string green
     *
     * @param s the string to color
     * @return the green string
     */
    public static String green(String s) {
        return LogColor.GREEN + s + ColorEnum.reset();
    }

    /**
     * Make a string red
     *
     * @param s the string to color
     * @return the red string
     */
    public static String red(String s) {
        return LogColor.RED + s + ColorEnum.reset();
    }

    /**
     * Make a string orange
     *
     * @param s the string to color
     * @return the orange string
     */
    public static String orange(String s) {
        return LogColor.ORANGE + s + ColorEnum.reset();
    }

    /**
     * Make a string blue
     *
     * @param s the string to color
     * @return the blue string
     */
    public static String blue(String s) {
        return LogColor.BLUE + s + ColorEnum.reset();
    }

    /**
     * Make a string yellow
     *
     * @param s the string to color
     * @return the yellow string
     */
    public static String yellow(String s) {
        return LogColor.YELLOW + s + ColorEnum.reset();
    }

    /**
     * Make a string pink
     *
     * @param s the string to color
     * @return the pink string
     */
    public static String pink(String s) {
        return LogColor.PINK + s + ColorEnum.reset();
    }

    /**
     * Make a string cyan
     *
     * @param s the string to color
     * @return the cyan string
     */
    public static String aqua(String s) {
        return LogColor.CYAN + s + ColorEnum.reset();
    }

    /**
     * Make a string purple
     *
     * @param s the string to color
     * @return the purple string
     */
    public static String purple(String s) {
        return LogColor.PURPLE + s + ColorEnum.reset();
    }

    /**
     * Make a string white
     *
     * @param s the string to color
     * @return the white string
     */
    public static String white(String s) {
        return LogColor.WHITE + s + ColorEnum.reset();
    }

    /**
     * Make a string gray
     *
     * @param s the string to color
     * @return the gray string
     */
    public static String gray(String s) {
        return LogColor.GRAY + s + ColorEnum.reset();
    }

    /**
     * Make a string dark gray
     *
     * @param s the string to color
     * @return the dark gray string
     */
    public static String darkGray(String s) {
        return LogColor.DARK_GRAY + s + ColorEnum.reset();
    }

    /**
     * Make a string magenta
     *
     * @param s the string to color
     * @return the string in magenta
     */
    public static String magenta(String s) {
        return LogColor.MAGENTA + s + ColorEnum.reset();
    }

    /**
     * Reset the color of the string
     *
     * @return the reset string
     */
    public static String reset() {
        return ColorEnum.reset();
    }

    /**
     * Make a string custom color.
     *
     * @param s the string to color
     * @param c the color to use. Use AWT Color.
     * @return the colored string
     */
    public static String custom(Color c, String s) {
        return custom(c.getRed(), c.getGreen(), c.getBlue(), s);
    }

    public static String custom(ColorEnum color, String s) {
        return custom(color.red(), color.green(), color.blue(), s);
    }
    public static String custom(int r, int g, int b, String s) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m" + s + reset();
    }
}
