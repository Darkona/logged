package com.darkona.logged.internals;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
public interface LogDecorator {

    String ornament(int width);

    String center(String s, int width);

    String fill(String s, int length);

    String rainbowify(String s);

    String green(String s);

    String red(String s);

    String yellow(String s);

    String blue(String s);

    String orange(String s);

    String pink(String s);

    String cyan(String s);

    String magenta(String s);

    String lightGray(String s);

    String white(String s);

    String darkGray(String s);

    String custom(int red, int green, int blue, String s);

    String reset();

    String mask(String s, @Nullable Integer unmasked, @Nullable Character mask);

    String mask(char[] bytes, @Nullable Integer unmasked, @Nullable Character mask);

    String daySuffix(int day);

    String capitalize(String s);

    String bannerize(String color, String s, int width);

    String bannerize(String s, int width);

    String clearColor(String s);

}
