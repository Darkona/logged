package com.darkona.logged.utils;

import com.darkona.logged.colors.ColorEnum;
import com.darkona.logged.colors.LogColor;
import jakarta.annotation.Nullable;

import java.awt.*;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SuppressWarnings("unused")
public class LogStrings {

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
    private static final String RESET = "\u001B[0m";
    private static final OutputStreamWriter writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
    public static boolean isUtf = true;
    static boolean enabled = false;

    private LogStrings() {}

    public static void enableUtf() {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        enabled = true;
    }

    /**
     * Create an ornament of a given width;
     *
     * @param width the width of the ornament
     * @return the ornament. Example: ||====================||
     */
    private static String ornament(int width) {
        return "||" + fill("=", width) + "||";
    }


    /**
     * Center a string in a given width
     *
     * @param s     the string to center
     * @param width the width to center the string in
     * @return the centered string
     */
    public static String center(String s, int width) {
        if (s == null) return "";
        var msg = cleanColor(s);
        return specialCenter(s, msg, width);
    }

    /**
     * Makes a string of a given length filled with a given character.
     *
     * @param s      the string to fill
     * @param length the length to fill the string to
     * @return the filled string
     */
    public static String fill(String s, int length) {
        return s.repeat(Math.max(0, length));
    }

    /**
     * Returns a rainbow color for a given index.
     *
     * @param i the index to get the color for. Must be between 0 and 6.
     * @return the rainbow color
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
     * Rainbowify a string, making each character a different color of the rainbow.
     *
     * @param s the string to rainbowify
     * @return the rainbowified string
     */
    public static String rainbow(String s) {
        if (!enabled) return s;
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
     * Make a string green
     *
     * @param s the string to color
     * @return the green string
     */
    public static String green(String s) {
        return enabled ? LogColor.GREEN + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string red
     *
     * @param s the string to color
     * @return the red string
     */
    public static String red(String s) {
        return enabled ? LogColor.RED + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string orange
     *
     * @param s the string to color
     * @return the orange string
     */
    public static String orange(String s) {
        return enabled ? LogColor.ORANGE + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string blue
     *
     * @param s the string to color
     * @return the blue string
     */
    public static String blue(String s) {
        return enabled ? LogColor.BLUE + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string yellow
     *
     * @param s the string to color
     * @return the yellow string
     */
    public static String yellow(String s) {
        return enabled ? LogColor.YELLOW + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string cyan
     *
     * @param s the string to color
     * @return the cyan string
     */
    public static String pink(String s) {
        return enabled ? LogColor.PINK + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string cyan
     *
     * @param s the string to color
     * @return the cyan string
     */
    public static String aqua(String s) {
        return enabled ? LogColor.CYAN + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string purple
     *
     * @param s the string to color
     * @return the purple string
     */
    public static String purple(String s) {
        return enabled ? LogColor.PURPLE + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string white
     *
     * @param s the string to color
     * @return the white string
     */
    public static String white(String s) {
        return enabled ? LogColor.WHITE + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string gray
     *
     * @param s the string to color
     * @return the gray string
     */
    public static String gray(String s) {
        return enabled ? LogColor.GRAY + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string dark gray
     *
     * @param s
     * @return the dark gray string
     */
    public static String darkGray(String s) {
        return enabled ? LogColor.DARK_GRAY + s + ColorEnum.reset() : s;
    }

    /**
     * Make a string custom color.
     *
     * @param s the string to color
     * @param c the color to use. Use AWT Color.
     * @return the colored string
     */
    public static String custom(Color c, String s) {
        return enabled ? "\u001B[38;2;" + c.getRed() + ";" + c.getGreen() + ";" + c.getBlue() + "m" + s + reset() : s;
    }

    public static String custom(int r, int g, int b, String s) {
        return enabled ? "\u001B[38;2;" + r + ";" + g + ";" + b + "m" + s + reset() : s;
    }

    /**
     * Reset the color of the string
     *
     * @return the reset string
     */
    public static String reset() {
        return enabled ? ColorEnum.reset() : "";
    }

    /**
     * Get a masked string. The string will be masked with a given character.
     *
     * @param string   the string to mask
     * @param unmasked the number of characters to leave unmasked
     * @param maskChar the character to use for masking
     * @return the masked string
     */
    public static String getMaskedString(String string, @Nullable Integer unmasked, @Nullable Character maskChar) {
        return getMaskedString(string.toCharArray(), unmasked, maskChar);
    }

    /**
     * Get a masked string. The string will be masked with a given character.
     *
     * @param bytes    the bytes to mask
     * @param unmasked the number of characters to leave unmasked
     * @param maskChar the character to use for masking
     * @return the masked string
     */
    public static String getMaskedString(char[] bytes, @Nullable Integer unmasked, @Nullable Character maskChar) {
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
     * Get the suffix for a given day. Example: 1st, 2nd, 3rd, 4th, 5th, 6th, 7th, 8th, 9th, 10th
     *
     * @param day the day to get the suffix for
     * @return the suffix
     */
    public static String getDaySuffix(int day) {
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


    public static String getSubStr(String str, int begin, int end) {
        if (str == null || str.isEmpty()) return "";
        return (end <= str.length() && begin < end && begin >= 0) ? str.substring(begin, end) : str;
    }

    public static String getSubStr(String str, int begin, String delimiter) {
        if (str == null || str.isEmpty()) return "";
        var trimmed = str.trim();
        var firstSpace = str.indexOf(delimiter);
        if (firstSpace == -1) {
            return trimmed;
        }
        return str.substring(begin, firstSpace);
    }

    /**
     * Capitalize a string. Example: capitalize("hello") -> "Hello"
     *
     * @param s the string to capitalize
     * @return the capitalized string
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
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
        return isUtf ?
               bannerizeInternal(color, s, width, u_d_ver, u_d_hor, u_d_top_l, u_d_top_r, u_d_bot_l, u_d_bot_r) :
               bannerizeInternal(color, s, width, d_ver, d_hor, d_cross, d_cross, d_cross, d_cross);
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
     *     ╔════════════════◄►════════════════╗
     *     ║              String              ║
     *     ╚════════════════◄►════════════════╝
     *  </pre>
     *
     * @param s     the string to bannerize
     * @param width the width of the banner
     * @return the string inside a banner
     */
    public static String bannerize(String s, int width) {
        return bannerize("", s, width);
    }

    private static String specialCenter(String s, String clean, int width) {
        if (s == null) return "";
        if (width <= clean.length()) return s;
        var half = (width - clean.length()) / 2;
        var space = (clean.length() % 2 == 0) ? (0 != width % 2) ? half + 1 : half - 1 : half;
        return fill(" ", half) + s + fill(" ", space);
    }

    public static String cleanColor(String s) {
        return s.replaceAll("\u001B\\[[\\d;]*[m;]", "");
    }

    private static String bannerizeInternal(String color, String s, int width, String v, String h, String topL, String topR, String botL, String botR) {
        StringBuilder sb = new StringBuilder();
        if (s == null) return "";
        var clean = cleanColor(s);
        if (clean.length() > width - 4) return s;

        var top = topL + fill(h, width / 2 - 2) + "◄►" + fill(h, width / 2 - 2) + topR;
        var bottom = botL + fill(h, width / 2 - 2) + "◄►" + fill(h, width / 2 - 2) + botR;

        if (!color.isEmpty()) sb.append(color);

        sb.append(top).append(System.lineSeparator());

        for (var line : s.split(System.lineSeparator())) {
            sb.append(v).append(" ");
            sb.append(specialCenter(line, clean, width - 2));
            if (!color.isEmpty()) sb.append(color);
            sb.append(v).append(" ");
            sb.append(System.lineSeparator());
        }

        sb.append(bottom);
        if (!color.isEmpty()) sb.append(RESET);
        return sb.toString();
    }

}
