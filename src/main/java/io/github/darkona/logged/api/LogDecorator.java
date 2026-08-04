package io.github.darkona.logged.api;

import io.github.darkona.logged.colors.ColorEnum;
import jakarta.annotation.Nullable;

/**
 * A decorator interface for enriching log messages with visual formatting and colors.
 * <p>
 * Implementations may apply ANSI color codes, padding, banners, or other text transformations
 * to make log output more readable and visually distinct.
 * </p>
 */
@SuppressWarnings("unused")
public interface LogDecorator {

    /**
     * Creates a decorative ornament line with the given width.
     *
     * @param width the total number of characters in the ornament
     * @return a string representing the ornament
     */
    String ornament(int width);

    /**
     * Centers the given string within a field of the specified width.
     *
     * @param s     the text to center
     * @param width the total width of the field
     * @return the centered text
     */
    String center(String s, int width);

    /**
     * Pads or truncates the given string to match the requested length.
     *
     * @param s      the string to fill
     * @param length the desired length
     * @return the padded or truncated string
     */
    String fill(String s, int length);

    /**
     * Applies a rainbow-like color effect to the given string.
     *
     * @param s the input string
     * @return the rainbow-colored string
     */
    String rainbowify(String s);

    /**
     * @return the input string decorated in green.
     */
    String green(String s);

    /**
     * @return the input string decorated in red.
     */
    String red(String s);

    /**
     * @return the input string decorated in yellow.
     */
    String yellow(String s);

    /**
     * @return the input string decorated in blue.
     */
    String blue(String s);

    /**
     * @return the input string decorated in orange.
     */
    String orange(String s);

    /**
     * @return the input string decorated in pink.
     */
    String pink(String s);

    /**
     * @return the input string decorated in cyan.
     */
    String cyan(String s);

    /**
     * @return the input string decorated in magenta.
     */
    String magenta(String s);

    /**
     * @return the input string decorated in light gray.
     */
    String lightGray(String s);

    /**
     * @return the input string decorated in white.
     */
    String white(String s);

    String purple(String e);

    /**
     * @return the input string decorated in dark gray.
     */
    String darkGray(String s);

    /**
     * Applies a custom RGB color to the given string.
     *
     * @param red   red component (0–255)
     * @param green green component (0–255)
     * @param blue  blue component (0–255)
     * @param s     the input string
     * @return the RGB-colored string
     */
    String custom(int red, int green, int blue, String s);

    /**
     * Applies a custom RGB background color to the given string.
     *
     * @param red   red component (0..255)
     * @param green green component (0..255)
     * @param blue  blue component (0..255)
     * @param s     the input string
     * @return the background-colored string
     */
    String customBg(int red, int green, int blue, String s);

    /**
     * Applies a predefined custom color to the given string.
     *
     * @param color the color enum
     * @param s     the input string
     * @return the colorized string
     */
    String custom(ColorEnum color, String s);

    /**
     * Resets any applied color or style, returning an ANSI reset sequence.
     *
     * @return the reset string
     */
    String reset();

    /**
     * Masks the given string for sensitive data logging.
     * <p>
     * Optionally leaves a number of characters unmasked and allows
     * customization of the mask character.
     * </p>
     *
     * @param s        the input string
     * @param unmasked number of trailing characters to leave unmasked (nullable)
     * @param mask     the masking character (nullable, defaults to '*')
     * @return the masked string
     */
    String mask(String s, @Nullable Integer unmasked, @Nullable Character mask);

    /**
     * Masks a character array for sensitive data logging.
     *
     * @param bytes    the input characters
     * @param unmasked number of trailing characters to leave unmasked (nullable)
     * @param mask     the masking character (nullable, defaults to '*')
     * @return the masked string
     */
    String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character mask);

    /**
     * Returns the appropriate ordinal suffix for a given day of the month.
     * <p>Example: 1 → "st", 2 → "nd", 3 → "rd", 4 → "th".</p>
     *
     * @param day the day of the month
     * @return the ordinal suffix
     */
    String daySuffix(int day);

    /**
     * Capitalizes the first letter of the given string.
     *
     * @param s the input string
     * @return the capitalized string
     */
    String capitalize(String s);

    /**
     * Creates a decorated banner with a given color and width.
     *
     * @param color the banner color
     * @param s     the text to display
     * @param width the banner width
     * @return the bannerized string
     */
    String bannerize(String color, String s, int width);

    /**
     * Creates a plain decorated banner with the given width.
     *
     * @param s     the text to display
     * @param width the banner width
     * @return the bannerized string
     */
    String bannerize(String s, int width);

    /**
     * Removes any color codes from the given string.
     *
     * @param s the colored string
     * @return the plain string without color codes
     */
    String clearColor(String s);

    // Additional methods from utility classes

    /**
     * Creates a decorated banner with a given color enum and width.
     *
     * @param color the banner color
     * @param s     the text to display
     * @param width the banner width
     * @return the bannerized string
     */
    String bannerize(ColorEnum color, String s, int width);

    /**
     * Creates a decorated banner with separate colors for border and text.
     *
     * @param bannerColor the border color
     * @param textColor   the text color
     * @param s           the text to display
     * @param width       the banner width
     * @return the bannerized string
     */
    String bannerize(ColorEnum bannerColor, ColorEnum textColor, String s, int width);

    /**
     * Creates a banner with optional border.
     *
     * @param s          the text to display
     * @param width      the banner width
     * @param withBorder whether to include border
     * @return the bannerized string
     */
    String bannerize(String s, int width, boolean withBorder);

    /**
     * Returns a safe String representation of the given object.
     *
     * @param o object to stringify
     * @return textual representation
     */
    String objectString(Object o);

    /**
     * Substring between indexes begin (inclusive) and end (exclusive).
     *
     * @param str   the string
     * @param begin start index
     * @param end   end index
     * @return the substring
     */
    String getSubstring(String str, int begin, int end);

    /**
     * Substring from begin up to the first occurrence of delimiter.
     *
     * @param str       the string
     * @param begin     start index
     * @param delimiter the delimiter
     * @return the substring
     */
    String getSubstringUntil(String str, int begin, String delimiter);

    /**
     * Truncates the string to at most max characters.
     *
     * @param s   input string
     * @param max maximum length
     * @return truncated string
     */
    String truncate(String s, int max);

    /**
     * Truncates by grapheme clusters.
     *
     * @param s           input string
     * @param maxClusters maximum number of graphemes
     * @return truncated string
     */
    String truncateGraphemes(String s, int maxClusters);

    /**
     * Applies a custom color using hexadecimal color code.
     *
     * @param hex the hexadecimal color string
     * @param s   the input string
     * @return the colored string
     */
    String colorizeHex(String hex, String s);

    /**
     * Applies a sequence of colors to each character.
     *
     * @param s      the string to colorize
     * @param colors the list of colors to cycle through
     * @return the colorized string
     */
    String colorizeChars(String s, java.util.List<ColorEnum> colors);
}
