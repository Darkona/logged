package io.github.darkona.logged.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BannerizerTest {

    @Test
    void centerPadsEvenly() {
        String centered = Bannerizer.center("Hello", 11);
        assertEquals("   Hello   ", centered);
    }

    @Test
    void centerHandlesNull() {
        assertEquals("", Bannerizer.center(null, 10));
    }

    @Test
    void ornamentProducesExpectedWidth() {
        String o = Bannerizer.ornament(5);
        assertEquals("||" + "=".repeat(5) + "||", o);
    }

    @Test
    void bannerizeContainsOriginalText() {
        String b = Bannerizer.bannerize("Hello", 30);
        assertTrue(b.contains("Hello"));
        assertTrue(b.contains(System.lineSeparator()));
    }

    @Test
    void bannerizeReturnsOriginalWhenTooWide() {
        String s = "012345678901234567890"; // 21 chars
        // width must be > len + 4 to render banner; pick a too-small width
        String b = Bannerizer.bannerize(s, 22);
        assertEquals(s, b);
    }

    @Test
    void clearColorRemovesAnsiSequences() {
        String colored = "\u001B[31mRED\u001B[0m plain";
        assertEquals("RED plain", Bannerizer.clearColor(colored));
    }
}

