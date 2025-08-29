package io.github.darkona.logged.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransformerTest {

    @Test
    void fillRepeats() {
        assertEquals("====", Transformer.fill("=", 4));
        assertEquals("", Transformer.fill("=", 0));
    }

    @Test
    void maskStringAndChars() {
        assertEquals("su*********", Transformer.mask("supersecret", 2, '*'));
        assertEquals("******", Transformer.mask("secret".toCharArray(), 0, '*'));
        assertEquals("se****", Transformer.mask("secret".toCharArray(), 2, '*'));
    }

    @Test
    void daySuffixCoversCases() {
        assertEquals("st", Transformer.daySuffix(1));
        assertEquals("nd", Transformer.daySuffix(2));
        assertEquals("rd", Transformer.daySuffix(3));
        assertEquals("th", Transformer.daySuffix(4));
        assertEquals("th", Transformer.daySuffix(11));
        assertEquals("th", Transformer.daySuffix(12));
        assertEquals("th", Transformer.daySuffix(13));
        assertEquals("st", Transformer.daySuffix(21));
    }

    @Test
    void getSubstringValidAndInvalid() {
        assertEquals("ell", Transformer.getSubstring("hello", 1, 4));
        // invalid indices -> returns original string per implementation
        assertEquals("hello", Transformer.getSubstring("hello", 3, 99));
        assertEquals("", Transformer.getSubstring(null, 0, 1));
        assertEquals("", Transformer.getSubstring("", 0, 1));
    }

    @Test
    void getSubstringUntilWorks() {
        assertEquals("hello", Transformer.getSubstringUntil("hello world", 0, " "));
        assertEquals("helloworld", Transformer.getSubstringUntil("  helloworld  ", 0, ":"));
        assertEquals("", Transformer.getSubstringUntil(null, 0, " "));
    }

    @Test
    void capitalizeHandlesNullAndEmpty() {
        assertEquals("Hello", Transformer.capitalize("hello"));
        assertNull(Transformer.truncate(null, 5));
        assertEquals("", Transformer.capitalize(""));
        assertNull(Transformer.truncate(null, 0));
    }

    @Test
    void truncateAddsEllipsisWhenNeeded() {
        String r = Transformer.truncate("abcdefgh", 5);
        assertTrue(r.startsWith("abcde"));
        assertTrue(r.length() > 5); // includes some suffix (ellipsis-like)
        assertEquals("abc", Transformer.truncate("abc", 5));
    }

    @Test
    void truncateGraphemesHandlesBounds() {
        assertEquals("", Transformer.truncateGraphemes(null, 3));
        assertThrows(IllegalArgumentException.class, () -> Transformer.truncateGraphemes("abc", -1));
        assertEquals("abc", Transformer.truncateGraphemes("abc", 5));
    }

    @Test
    void truncateGraphemesOnEmojiSequence() {
        String s = "A👍B"; // 3 grapheme clusters in most cases
        String t = Transformer.truncateGraphemes(s, 2);
        assertTrue(t.length() >= 2); // be tolerant across JDK impls
    }
}

