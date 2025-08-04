package com.darkona.logged;

import com.darkona.logged.colors.AnsiColor;
import jakarta.annotation.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

public class PlainLogDecorator implements LogDecorator {


    @Override
    public String ornament(int width) {
        return LogStrings.ornament(width);
    }

    @Override
    public String center(String s, int width) {
        return LogStrings.center(s, width);
    }

    @Override
    public String fill(String s, int length) {
        return LogStrings.fill(s, length);
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
    public String darkGray(String s) {
        return s;
    }

    @Override
    public String custom(int red, int green, int blue, String s) {
        return s;
    }

    @Override
    public String reset() {
        return "";
    }

    @Override
    public String mask(String s, @Nullable Integer unmasked, @Nullable Character mask) {
        return LogStrings.mask(s, unmasked, mask);
    }

    @Override
    public String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character mask) {
        return LogStrings.mask(bytes, unmasked, mask);
    }

    @Override
    public String daySuffix(int day) {
        return LogStrings.daySuffix(day);
    }

    @Override
    public String capitalize(String s) {
        return LogStrings.capitalize(s);
    }

    @Override
    public String bannerize(String color, String s, int width) {
        return LogStrings.bannerize(s, width);
    }

    @Override
    public String bannerize(String s, int width) {
        return LogStrings.bannerize(s, width);
    }

    @Override
    public String clearColor(String s) {
        return s;
    }
}
