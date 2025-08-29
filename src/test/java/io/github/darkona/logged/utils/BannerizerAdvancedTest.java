package io.github.darkona.logged.utils;

import io.github.darkona.logged.colors.LogColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BannerizerAdvancedTest {

    @BeforeEach
    void forceAscii() { Bannerizer.overrideUtf8(false); }

    @AfterEach
    void clearOverride() { Bannerizer.overrideUtf8(null); }

    @Test
    void mapTablerizeNoColorAscii() {
        Map<String,String> data = new LinkedHashMap<>();
        data.put("user", "ana");
        data.put("role", "admin");
        String out = Bannerizer.mapTablerize(new String[]{"Key","Value"}, data, 40);
        String[] lines = out.split("\r?\n");
        assertTrue(lines[0].startsWith("+")); // top border ASCII
        assertTrue(out.contains("Key"));
        assertTrue(out.contains("Value"));
        assertTrue(out.contains("user"));
        assertTrue(out.contains("admin"));
        assertTrue(out.contains("|")); // vertical borders
    }

    @Test
    void mapTablerizeWithColorsAndBorder() {
        Map<String,String> data = new LinkedHashMap<>();
        data.put("user", "ana");
        String out = Bannerizer.mapTablerize(LogColor.BLUE, LogColor.RED, LogColor.GREEN, LogColor.CYAN,
                new String[]{"K","V"}, data, 30);
        assertTrue(out.contains(LogColor.BLUE.toString())); // header
        assertTrue(out.contains(LogColor.CYAN.toString())); // border
        assertTrue(out.contains(LogColor.RED.toString()));  // col1
        assertTrue(out.contains(LogColor.GREEN.toString())); // col2
    }

    @Test
    void bannerizeSeparateColors() {
        String out = Bannerizer.bannerize(LogColor.RED, LogColor.GREEN, "Hello", 24);
        String[] lines = out.split("\r?\n");
        assertTrue(lines.length >= 3);
        assertTrue(lines[0].contains(LogColor.RED.toString())); // top border colored
        assertTrue(out.contains(LogColor.GREEN.toString())); // text colored
    }

    @Test
    void menuWithColors() {
        String out = Bannerizer.menu(LogColor.MAGENTA, LogColor.WHITE, List.of("Start","Exit"), 24);
        assertTrue(out.contains("1) "));
        assertTrue(out.contains("Start"));
        assertTrue(out.contains(LogColor.MAGENTA.toString()));
        assertTrue(out.contains(LogColor.WHITE.toString()));
    }
}

