package io.github.darkona.logged.utils;

import io.github.darkona.logged.colors.Blue;
import io.github.darkona.logged.colors.ColorEnum;
import io.github.darkona.logged.colors.Green;
import io.github.darkona.logged.colors.Red;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColorizerTest {

    @Test
    void rainbowColorBounds() {
        assertEquals("", Colorizer.rainbowColor(-1));
        assertEquals("", Colorizer.rainbowColor(7));
        assertNotEquals("", Colorizer.rainbowColor(0));
        assertNotEquals("", Colorizer.rainbowColor(6));
    }

    @Test
    void rainbowifyAppendsReset() {
        String r = Colorizer.rainbowify("Hello");
        assertTrue(r.endsWith(ColorEnum.reset()));
        assertTrue(r.contains("H"));
    }

    @Test
    void colorizeCharsCyclesColors() {
        String r = Colorizer.colorizeChars("Hi", List.of(Red.RED, Green.GREEN));
        assertTrue(r.startsWith(Red.RED.toString()));
        assertTrue(r.contains("H"));
        assertTrue(r.endsWith(ColorEnum.reset()));
    }

    @Test
    void simpleColorsWrapAndReset() {
        assertTrue(Colorizer.green("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.red("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.blue("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.yellow("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.white("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.purple("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.pink("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.aqua("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.gray("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.lightGray("x").endsWith(ColorEnum.reset()));
        assertTrue(Colorizer.magenta("x").endsWith(ColorEnum.reset()));
        assertEquals(ColorEnum.reset(), Colorizer.reset());
    }

    @Test
    void customHexAndRgbProduceEscapeCodes() {
        String rgb = Colorizer.custom(255, 54, 116, "Hi");
        assertTrue(rgb.startsWith("\u001B[38;2;255;54;116m"));
        assertTrue(rgb.endsWith(ColorEnum.reset()));

        String hex = Colorizer.colorizeHex("#ff3674", "Hi");
        assertTrue(hex.startsWith("\u001B[38;2;255;54;116m"));
    }

    @Test
    void customBgProducesEscapeCodes() {
        String bg = Colorizer.customBg(1, 2, 3, "x");
        assertTrue(bg.startsWith("\u001B[48;2;1;2;3m"));
        assertTrue(bg.endsWith(ColorEnum.reset()));
    }

    @Test
    void customBgClampsValues() {
        String bg = Colorizer.customBg(300, -5, 10, "x");
        assertTrue(bg.startsWith("\u001B[48;2;255;0;10m"));
    }

    @Test
    void rainbowifyResetsOnNewline() {
        String out = Colorizer.rainbowify("A\nB");
        String red = io.github.darkona.logged.colors.LogColor.RED.toString();
        int newline = out.indexOf('\n');
        assertTrue(newline > 0);
        String after = out.substring(newline + 1);
        assertTrue(after.startsWith(red + "B"));
    }

    @Test
    void customColorEnumAndAwtColor() {
        String s1 = Colorizer.custom(Blue.BLUE, "X");
        assertTrue(s1.contains("X"));
        assertTrue(s1.endsWith(ColorEnum.reset()));

        String s2 = Colorizer.custom(new Color(10, 20, 30), "Y");
        assertTrue(s2.startsWith("\u001B[38;2;10;20;30m"));
        assertTrue(s2.endsWith(ColorEnum.reset()));
    }
}
