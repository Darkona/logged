package io.github.darkona.logged.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StringInterpolatorTest {

    @Test
    void interpolateReplacesKnownKeys() {
        String t = "Hello {name}, welcome to {lang}!";
        String r = StringInterpolator.interpolate(t, Map.of("name", "Javi", "lang", "Java"));
        assertEquals("Hello Javi, welcome to Java!", r);
    }

    @Test
    void interpolateLeavesUnknownPlaceholders() {
        String t = "Hello {name}, use {unknown}!";
        String r = StringInterpolator.interpolate(t, Map.of("name", "Javi"));
        assertEquals("Hello Javi, use {unknown}!", r);
    }

    @Test
    void interpolateStrictThrowsOnMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> StringInterpolator.interpolateStrict("Hi {who}", Map.of()));
    }

    @Test
    void interpolateStrictWorksWhenAllPresent() {
        String r = StringInterpolator.interpolateStrict("Hi {who}", Map.of("who", "there"));
        assertEquals("Hi there", r);
    }

    @Test
    void interpolateWithDefaultsFallsBack() {
        String r = StringInterpolator.interpolateWithDefaults("Hello {name:guest}", Map.of());
        assertEquals("Hello guest", r);
    }

    @Test
    void interpolateWithDefaultsOverridesWhenPresent() {
        String r = StringInterpolator.interpolateWithDefaults("Lang: {lang:Java}", Map.of("lang", "Rust"));
        assertEquals("Lang: Rust", r);
    }

    @Test
    void escapingDoubleBracesAreRendered() {
        String t = "Use {{ and }} to show braces";
        String r = StringInterpolator.interpolate(t, Map.of());
        assertEquals("Use { and } to show braces", r);
    }

    @Test
    void escapingWorksAlongsideTokens() {
        String t = "Cost: {{ {price} }}";
        String r = StringInterpolator.interpolate(t, Map.of("price", "10"));
        assertEquals("Cost: { 10 }", r);
    }

    @Test
    void defaultsSupportColonsInValue() {
        String t = "Val: {k:a:b:c}";
        String r = StringInterpolator.interpolateWithDefaults(t, Map.of());
        assertEquals("Val: a:b:c", r);
    }

    @Test
    void unmatchedOpenBraceLeftAsLiteral() {
        String t = "Hello {name";
        String r = StringInterpolator.interpolate(t, Map.of("name", "Javi"));
        assertEquals("Hello {name", r);
    }

    @Test
    void repeatedUseShouldStayConsistent() {
        String t = "{greet}, {name}!";
        for (int i = 0; i < 1000; i++) {
            String r = StringInterpolator.interpolate(t, Map.of("greet", "Hi", "name", "Javi"));
            assertEquals("Hi, Javi!", r);
        }
    }
}
