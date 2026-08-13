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

    @Test
    void compiledTemplateMatchesInterpolate() {
        String t = "Hello {name}, use {unknown}, {{escaped}}!";
        Map<String, String> values = Map.of("name", "Javi");
        var compiled = StringInterpolator.compile(t);
        assertEquals(StringInterpolator.interpolate(t, values), compiled.render(values));
    }

    @Test
    void compiledTemplateWithDefaultsMatchesInterpolateWithDefaults() {
        String t = "Hello {name:guest}, val {k:a:b:c}, lang {lang:Java}";
        Map<String, String> values = Map.of("lang", "Rust");
        var compiled = StringInterpolator.compileWithDefaults(t);
        assertEquals(StringInterpolator.interpolateWithDefaults(t, values), compiled.render(values));
    }

    @Test
    void compiledTemplateIsReusable() {
        var compiled = StringInterpolator.compile("{greet}, {name}!");
        assertEquals("Hi, Javi!", compiled.render(Map.of("greet", "Hi", "name", "Javi")));
        assertEquals("Hola, Ana!", compiled.render(Map.of("greet", "Hola", "name", "Ana")));
    }

    @Test
    void compileNullOrEmptyRendersEmpty() {
        assertTrue(StringInterpolator.compile(null).isEmpty());
        assertTrue(StringInterpolator.compileWithDefaults("").isEmpty());
        assertEquals("", StringInterpolator.compile(null).render(Map.of("a", "b")));
        assertEquals("", StringInterpolator.compileWithDefaults("").render(null));
    }

    @Test
    void compiledTemplateToleratesNullValuesMap() {
        assertEquals("Hi {name}", StringInterpolator.compile("Hi {name}").render(null));
    }
}
