package io.github.darkona.logged.utils;

import io.github.darkona.logged.colors.ColorEnum;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating styled console banners, ornaments, and centered text output
 * with minimal CPU/GC overhead. Provides ASCII and UTF box-drawing skins, ANSI‑aware centering
 * and a linear ANSI clearer.
 */
@SuppressWarnings("unused")
public class Bannerizer {

    // Skin for borders (ASCII / UTF box-drawing)
    private record Skin(String topLeft, String topRight,
                        String bottomLeft, String bottomRight,
                        String horizontal, String vertical,
                        String joinLeft, String joinCross,
                        String joinRight, String topJoin, String bottomJoin) {

    }

    private static final Skin ASCII = new Skin("+", "+", "+", "+", "-", "|", "+",
            "+", "+", "+", "+");
    private static final Skin UTF = new Skin("╔", "╗", "╚", "╝", "═", "║", "╠",
            "╬", "╣", "╦", "╩");

    // Optional override for UTF detection; null => auto-detect from System.out
    private static volatile Boolean forceUtf8 = null;

    private static boolean supportsUtf8() {
        return forceUtf8 != null ? forceUtf8 : StandardCharsets.UTF_8.equals(System.out.charset());
    }
    public static void overrideUtf8(Boolean useUtf8) { forceUtf8 = useUtf8; }

    private Bannerizer() {}

    /** Creates an ornament line like ||=======|| */
    public static String ornament(int width) {
        return "||" + Transformer.fill("=", Math.max(0, width)) + "||";
    }

    /** Centers a single line (ANSI‑aware). */
    public static String center(String s, int width) {
        if (s == null) return "";
        var clean = clearColor(s);
        return specialCenter(s, clean, width);
    }

    /** Banner with color code prefix (may be empty), auto skin. */
    public static String bannerize(String color, String s, int width) {
        Skin skin = supportsUtf8() ? UTF : ASCII;
        return bannerize(color, s, width, skin);
    }

    /** Banner overload using ColorEnum (applies as a prefix). */
    public static String bannerize(ColorEnum color, String s, int width) {
        return bannerize(color != null ? color.toString() : "", s, width);
    }

    /** Banner with separate colors for border and text. */
    public static String bannerize(ColorEnum bannerColor, ColorEnum textColor, String s, int width) {
        String bc = bannerColor != null ? bannerColor.toString() : "";
        String tc = textColor != null ? textColor.toString() : "";
        Skin skin = currentSkin();
        return bannerize(bc, tc, s, width, skin);
    }

    /** Banner without color. */
    public static String bannerize(String s, int width) { return bannerize("", s, width); }

    /** Centers lines to width without borders. */
    public static String bannerize(String s, int width, boolean withBorder) {
        if (withBorder) return bannerize(s, width);
        if (s == null) return "";
        var lines = splitLines(s);
        var sb = new StringBuilder(s.length() + Math.max(0, lines.length * width));
        for (String line : lines) {
            var clean = clearColor(line);
            if (clean.length() >= width) {
                sb.append(line).append(System.lineSeparator());
                continue;
            }
            int left = (width - clean.length()) / 2;
            int right = width - clean.length() - left;
            sb.append(Transformer.fill(" ", left)).append(line).append(Transformer.fill(" ", right)).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    /**
     * Core bannerizer using the provided skin. Preserves ANSI in the payload and centers per line
     * based on visible (ANSI‑stripped) length. Returns the original text if any line exceeds width.
     */
    private static String bannerize(String color, String s, int width, Skin skin) {
        if (s == null) return "";
        int inner = Math.max(0, width - 2);
        String[] lines = splitLines(s);
        // Validate fit
        for (String line : lines) {
            if (clearColor(line).length() > inner) return s;
        }

        StringBuilder sb = new StringBuilder(s.length() + Math.max(0, width * (lines.length + 2)));
        if (!color.isEmpty()) sb.append(color);
        // Top
        sb.append(skin.topLeft).append(Transformer.fill(skin.horizontal, inner)).append(skin.topRight)
          .append(System.lineSeparator());
        // Lines
        for (String line : lines) {
            String clean = clearColor(line);
            int left = (inner - clean.length()) / 2;
            int right = inner - clean.length() - left;
            sb.append(skin.vertical)
              .append(Transformer.fill(" ", left)).append(line).append(Transformer.fill(" ", right))
              .append(skin.vertical)
              .append(System.lineSeparator());
        }
        // Bottom
        sb.append(skin.bottomLeft).append(Transformer.fill(skin.horizontal, inner)).append(skin.bottomRight);
        if (!color.isEmpty()) sb.append(Colorizer.reset());
        return sb.toString();
    }


    private static String bannerize(String borderColor, String textColor, String s, int width, Skin skin) {
        if (s == null) return "";
        int inner = Math.max(0, width - 2);
        String[] lines = splitLines(s);
        for (String line : lines) if (clearColor(line).length() > inner) return s;
        StringBuilder sb = new StringBuilder(s.length() + Math.max(0, width * (lines.length + 2)));
        // Top
        if (!borderColor.isEmpty()) sb.append(borderColor);
        sb.append(skin.topLeft).append(Transformer.fill(skin.horizontal, inner)).append(skin.topRight);
        if (!borderColor.isEmpty()) sb.append(Colorizer.reset());
        sb.append(System.lineSeparator());
        // Lines
        for (String line : lines) {
            String clean = clearColor(line);
            int left = (inner - clean.length()) / 2;
            int right = inner - clean.length() - left;
            if (!borderColor.isEmpty()) sb.append(borderColor);
            sb.append(skin.vertical);
            if (!borderColor.isEmpty()) sb.append(Colorizer.reset());
            if (!textColor.isEmpty()) sb.append(textColor);
            sb.append(Transformer.fill(" ", left)).append(line).append(Transformer.fill(" ", right));
            if (!textColor.isEmpty()) sb.append(Colorizer.reset());
            if (!borderColor.isEmpty()) sb.append(borderColor);
            sb.append(skin.vertical);
            if (!borderColor.isEmpty()) sb.append(Colorizer.reset());
            sb.append(System.lineSeparator());
        }
        // Bottom
        if (!borderColor.isEmpty()) sb.append(borderColor);
        sb.append(skin.bottomLeft).append(Transformer.fill(skin.horizontal, inner)).append(skin.bottomRight);
        if (!borderColor.isEmpty()) sb.append(Colorizer.reset());
        return sb.toString();
    }

    /** Centers ANSI‑formatted text using a pre‑cleaned visible version. */
    public static String specialCenter(String s, String clean, int width) {
        if (s == null) return "";
        if (width <= clean.length()) return s;
        int left = (width - clean.length()) / 2;
        int right = width - clean.length() - left;
        return Transformer.fill(" ", left) + s + Transformer.fill(" ", right);
    }

    /** Linear ANSI clearer: removes CSI sequences ESC[ ... final‑byte. */
    public static String clearColor(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder out = new StringBuilder(s.length());
        int i = 0, n = s.length();
        while (i < n) {
            char ch = s.charAt(i);
            if (ch == '\u001B' && (i + 1) < n && s.charAt(i + 1) == '[') { // CSI
                i += 2;
                while (i < n) {
                    char c = s.charAt(i++);
                    if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) break; // final byte
                }
                continue;
            }
            out.append(ch);
            i++;
        }
        return out.toString();
    }

    private static String[] splitLines(String s) { return s.split("\r?\n", -1); }

    private static Skin currentSkin() { return supportsUtf8() ? UTF : ASCII; }

    private static int visibleLen(String s) { return clearColor(s).length(); }

    private static String padRight(String s, int width) {
        int len = visibleLen(s);
        if (len == width) return s;
        if (len > width) return Transformer.truncate(clearColor(s), width);
        return s + Transformer.fill(" ", width - len);
    }

    /**
     * Renders a 2-column table using current skin with headers and a map of key/value rows (no colors).
     */
    public static String mapTablerize(String[] headers, Map<String, String> data, int width) {
        return mapTablerize("", "", "", "", headers, data, width);
    }

    /**
     * Renders a 2-column table using current skin with header color and per-column colors.
     */
    public static String mapTablerize(ColorEnum headerColor, ColorEnum col1Color, ColorEnum col2Color,
                                      String[] headers, Map<String, String> data, int width) {
        String hc = headerColor != null ? headerColor.toString() : "";
        String c1 = col1Color != null ? col1Color.toString() : "";
        String c2 = col2Color != null ? col2Color.toString() : "";
        return mapTablerize(hc, c1, c2, "", headers, data, width);
    }

    /**
     * Table with header color, column colors and border color.
     */
    public static String mapTablerize(ColorEnum headerColor, ColorEnum col1Color, ColorEnum col2Color, ColorEnum borderColor,
                                      String[] headers, Map<String, String> data, int width) {
        String hc = headerColor != null ? headerColor.toString() : "";
        String c1 = col1Color != null ? col1Color.toString() : "";
        String c2 = col2Color != null ? col2Color.toString() : "";
        String bc = borderColor != null ? borderColor.toString() : "";
        return mapTablerize(hc, c1, c2, bc, headers, data, width);
    }

    private static String mapTablerize(String headerColor, String col1Color, String col2Color, String borderColor,
                                       String[] headers, Map<String, String> data, int width) {


        Skin skin = currentSkin();

        String h1 = (headers != null && headers.length > 0 && headers[0] != null) ? headers[0] : "Key";
        String h2 = (headers != null && headers.length > 1 && headers[1] != null) ? headers[1] : "Value";
        int headerLen = h1.length() + h2.length();
        if (width < headerLen + 8) return "";
        int inner = Math.max(0, width - 2);

        int textAvail = Math.max(1, inner - 1 - 4);
        int maxKey = visibleLen(h1);
        int maxVal = visibleLen(h2);
        if (data != null) {
            for (var e : data.entrySet()) {
                maxKey = Math.max(maxKey, visibleLen(e.getKey()));
                maxVal = Math.max(maxVal, visibleLen(e.getValue()));
            }
        }

        int left = Math.max(1, Math.min(textAvail / 2, maxKey));
        int right = Math.max(1, textAvail - left);

        StringBuilder sb = new StringBuilder(width * (2 + (data != null ? data.size() : 0)) + 64);

        appendTopBorder(sb, skin, borderColor, left, right);

        appendRow(sb, skin, borderColor, h1, left, headerColor, h2, right, headerColor);

        appendHeaderSeparator(sb, skin, borderColor, left, right);

        if (data != null) {
            for (var e : data.entrySet()) {
                appendRow(sb, skin, borderColor,
                          e.getKey(), left, (col1Color != null ? col1Color : ""),
                          e.getValue(), right, (col2Color != null ? col2Color : ""));
            }
        }

        appendBottomBorder(sb, skin, borderColor, left, right);

        return sb.toString();
    }

    private static void beginColor(StringBuilder sb, String color) {
        if (color != null && !color.isEmpty()) sb.append(color);
    }

    private static void endColor(StringBuilder sb, String color) {
        if (color != null && !color.isEmpty()) sb.append(Colorizer.reset());
    }

    private static void appendVertical(StringBuilder sb, Skin skin, String borderColor) {
        beginColor(sb, borderColor);
        sb.append(skin.vertical);
        endColor(sb, borderColor);
    }

    private static void appendCell(StringBuilder sb, String content, int width, String color) {
        sb.append(' ');
        beginColor(sb, color);
        sb.append(padRight(content != null ? content : "", width));
        endColor(sb, color);
        sb.append(' ');
    }

    private static void appendRow(StringBuilder sb, Skin skin, String borderColor,
                                  String c1, int w1, String c1Color,
                                  String c2, int w2, String c2Color) {
        appendVertical(sb, skin, borderColor);
        appendCell(sb, c1, w1, c1Color);
        appendVertical(sb, skin, borderColor);
        appendCell(sb, c2, w2, c2Color);
        appendVertical(sb, skin, borderColor);
        sb.append(System.lineSeparator());
    }

    private static void appendTopBorder(StringBuilder sb, Skin skin, String borderColor, int left, int right) {
        beginColor(sb, borderColor);
        String H = skin.horizontal;
        int leftSpan = 1 + left + 1;   // space + content + space
        int rightSpan = 1 + right + 1; // space + content + space
        sb.append(skin.topLeft)
          .append(Transformer.fill(H, leftSpan))
          .append(skin.topJoin)
          .append(Transformer.fill(H, rightSpan))
          .append(skin.topRight);
        endColor(sb, borderColor);
        sb.append(System.lineSeparator());
    }

    private static void appendHeaderSeparator(StringBuilder sb, Skin skin, String borderColor, int left, int right) {
        beginColor(sb, borderColor);
        String H = skin.horizontal;
        int leftSpan = 1 + left + 1;
        int rightSpan = 1 + right + 1;
        sb.append(skin.joinLeft)
          .append(Transformer.fill(H, leftSpan))
          .append(skin.joinCross)
          .append(Transformer.fill(H, rightSpan))
          .append(skin.joinRight)
          .append(System.lineSeparator());
        endColor(sb, borderColor);
    }

    private static void appendBottomBorder(StringBuilder sb, Skin skin, String borderColor, int left, int right) {
        beginColor(sb, borderColor);
        String HB = skin.horizontal;
        int bLeftSpan = 1 + left + 1;
        int bRightSpan = 1 + right + 1;
        sb.append(skin.bottomLeft)
          .append(Transformer.fill(HB, bLeftSpan))
          .append(skin.bottomJoin)
          .append(Transformer.fill(HB, bRightSpan))
          .append(skin.bottomRight);
        endColor(sb, borderColor);
    }



    // ===== Menus =====

    /** Simple menu (numbered) with current skin and no colors. */
    public static String menu(List<String> items, int width) {
        return menu(null, null, items, width);
    }

    /** Colorized menu: number color and item color (optional). */
    public static String menu(ColorEnum numberColor, ColorEnum itemColor, List<String> items, int width) {
        Skin skin = currentSkin();
        if (items == null || items.isEmpty()) return "";
        int inner = Math.max(0, width - 2);
        int idxWidth = String.valueOf(items.size()).length() + 2; // e.g., "12) "
        int textWidth = Math.max(1, inner - idxWidth);
        String ncol = numberColor != null ? numberColor.toString() : "";
        String icol = itemColor != null ? itemColor.toString() : "";

        StringBuilder sb = new StringBuilder(width * (items.size() + 2));
        sb.append(skin.topLeft).append(Transformer.fill(skin.horizontal, inner)).append(skin.topRight)
          .append(System.lineSeparator());
        for (int i = 0; i < items.size(); i++) {
            String num = (i + 1) + ") ";
            String text = items.get(i) != null ? items.get(i) : "";
            // Build line
            sb.append(skin.vertical);
            // number
            if (!ncol.isEmpty()) sb.append(ncol);
            sb.append(padRight(num, idxWidth));
            if (!ncol.isEmpty()) sb.append(Colorizer.reset());
            // item
            if (!icol.isEmpty()) sb.append(icol);
            sb.append(padRight(text, textWidth));
            if (!icol.isEmpty()) sb.append(Colorizer.reset());
            sb.append(skin.vertical).append(System.lineSeparator());
        }
        sb.append(skin.bottomLeft).append(Transformer.fill(skin.horizontal, inner)).append(skin.bottomRight);
        return sb.toString();
    }
}
