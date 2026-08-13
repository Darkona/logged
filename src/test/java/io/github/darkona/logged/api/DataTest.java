package io.github.darkona.logged.api;

import io.github.darkona.logged.utils.StringInterpolator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DataTest {

    private static Data data(Map<LogToken, String> tokens) {
        return new Data(tokens, new Arg[0], 0L, 0, Collections.emptySet());
    }

    @Test
    void constructorAcceptsNullTokenValues() {
        Map<LogToken, String> tokens = new HashMap<>();
        tokens.put(LogToken.EXCEPTION_MESSAGE, null);
        tokens.put(LogToken.METHOD_NAME, "doWork");

        Data d = assertDoesNotThrow(() -> data(tokens));
        assertEquals("", d.get(LogToken.EXCEPTION_MESSAGE));
        assertEquals("", d.get(LogToken.EXCEPTION_MESSAGE.token()));
        assertEquals("doWork", d.get(LogToken.METHOD_NAME));
    }

    @Test
    void constructorAcceptsNullMap() {
        Data d = assertDoesNotThrow(() -> data(null));
        assertEquals("", d.get(LogToken.METHOD_NAME));
    }

    @Test
    void addTokenNormalizesNullToEmpty() {
        Data d = data(new HashMap<>());
        d.addToken(LogToken.FILENAME, null);
        assertEquals("", d.get(LogToken.FILENAME));
        assertEquals("", d.get(LogToken.FILENAME.token()));
        assertEquals("", d.tokens().get(LogToken.FILENAME));
    }

    @Test
    void addFlexTokenNormalizesNullToEmpty() {
        Data d = data(new HashMap<>());
        d.addFlexToken("custom", null);
        assertEquals("", d.get("custom"));
        assertEquals("", d.tok().get("custom"));
    }

    @Test
    void getReturnsEmptyForAbsentKeys() {
        Data d = data(new HashMap<>());
        assertEquals("", d.get("missing"));
        assertEquals("", d.get(LogToken.RETURN_VALUE));
    }

    @Test
    void tokViewReflectsAddToken() {
        Data d = data(new HashMap<>());
        d.addToken(LogToken.METHOD_NAME, "run");
        assertEquals("run", d.tok().get(LogToken.METHOD_NAME.token()));
    }

    @Test
    void nullValuesRenderEmptyInTemplatesNotLiteralNull() {
        Data d = data(new HashMap<>());
        d.addToken(LogToken.EXCEPTION_MESSAGE, null);
        String rendered = StringInterpolator.interpolateWithDefaults("{eM:none}", d.tok());
        assertEquals("", rendered);
    }
}
