package io.github.darkona.logged.internals;

import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.colors.ColorEnum;
import io.github.darkona.logged.utils.Bannerizer;
import io.github.darkona.logged.utils.Transformer;
import jakarta.annotation.Nullable;

public class PlainLogDecorator implements LogDecorator {

    @Override
    public String ornament(int width) {
        return Bannerizer.ornament(width);
    }

    @Override
    public String center(String s, int width) {
        return Bannerizer.center(s, width);
    }

    @Override
    public String fill(String s, int length) {
        return Transformer.fill(s, length);
    }

    @Override
    public String rainbowify(String s) {
        return s;
    }

    @Override
    public String green(String s) {
        return s;
    }

    @Override
    public String red(String s) {
        return s;
    }

    @Override
    public String yellow(String s) {
        return s;
    }

    @Override
    public String blue(String s) {
        return s;
    }

    @Override
    public String orange(String s) {
        return s;
    }

    @Override
    public String pink(String s) {
        return s;
    }

    @Override
    public String cyan(String s) {
        return s;
    }

    @Override
    public String magenta(String s) {
        return s;
    }

    @Override
    public String lightGray(String s) {
        return s;
    }

    @Override
    public String white(String s) {
        return s;
    }

    @Override
    public String purple(String e) {
        return e;
    }

    @Override
    public String darkGray(String s) {
        return s;
    }

    @Override
    public String custom(int red, int green, int blue, String s) {
        return s;
    }

    @Override
    public String customBg(int red, int green, int blue, String s) {
        return s;
    }

    @Override
    public String custom(ColorEnum color, String s) {
        return s;
    }

    @Override
    public String reset() {
        return "";
    }

    @Override
    public String mask(String s, @Nullable Integer unmasked, @Nullable Character mask) {
        return Transformer.mask(s, unmasked, mask);
    }

    @Override
    public String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character mask) {
        return Transformer.mask(bytes, unmasked, mask);
    }

    @Override
    public String daySuffix(int day) {
        return Transformer.daySuffix(day);
    }

    @Override
    public String capitalize(String s) {
        return Transformer.capitalize(s);
    }

    @Override
    public String bannerize(String color, String s, int width) {
        return Bannerizer.bannerize(s, width);
    }

    @Override
    public String bannerize(String s, int width) {
        return Bannerizer.bannerize(s, width);
    }

    @Override
    public String clearColor(String s) {
        return Bannerizer.clearColor(s);
    }

    @Override
    public String bannerize(ColorEnum color, String s, int width) {
        return Bannerizer.bannerize(s, width);  // Ignore color in plain mode
    }

    @Override
    public String bannerize(ColorEnum bannerColor, ColorEnum textColor, String s, int width) {
        return Bannerizer.bannerize(s, width);  // Ignore colors in plain mode
    }

    @Override
    public String bannerize(String s, int width, boolean withBorder) {
        return Bannerizer.bannerize(s, width, withBorder);
    }

    @Override
    public String objectString(Object o) {
        return Transformer.objectString(o);
    }

    @Override
    public String getSubstring(String str, int begin, int end) {
        return Transformer.getSubstring(str, begin, end);
    }

    @Override
    public String getSubstringUntil(String str, int begin, String delimiter) {
        return Transformer.getSubstringUntil(str, begin, delimiter);
    }

    @Override
    public String truncate(String s, int max) {
        return Transformer.truncate(s, max);
    }

    @Override
    public String truncateGraphemes(String s, int maxClusters) {
        return Transformer.truncateGraphemes(s, maxClusters);
    }

    @Override
    public String colorizeHex(String hex, String s) {
        return s;  // No coloring in plain mode
    }

    @Override
    public String colorizeChars(String s, java.util.List<ColorEnum> colors) {
        return s;  // No coloring in plain mode
    }
}
