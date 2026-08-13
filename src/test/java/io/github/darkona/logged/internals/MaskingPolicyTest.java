package io.github.darkona.logged.internals;

import io.github.darkona.logged.LoggedProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaskingPolicyTest {

    private static MaskingPolicy policy(LoggedProperties props) {
        var p = new MaskingPolicy(props);
        p.init();
        return p;
    }

    @Test
    void annotationPatternMatches() {
        var policy = policy(new LoggedProperties());
        assertTrue(policy.matchesPattern("card 4111111111111111", List.of("\\d{16}")));
        assertFalse(policy.matchesPattern("no digits here", List.of("\\d{16}")));
    }

    @Test
    void invalidAnnotationPatternIsIgnoredWithoutThrowing() {
        var policy = policy(new LoggedProperties());
        assertDoesNotThrow(() -> {
            assertFalse(policy.matchesPattern("anything", List.of("[unclosed")));
            // Negative cache: second use of the same bad pattern still just returns false
            assertFalse(policy.matchesPattern("anything", List.of("[unclosed")));
        });
        assertEquals(1, policy.cachedPatternCount());
    }

    @Test
    void annotationPatternCacheIsBounded() {
        var props = new LoggedProperties();
        props.getCache().setMaxAnnotationPatterns(2);
        var policy = policy(props);

        policy.matchesPattern("x", List.of("a+"));
        policy.matchesPattern("x", List.of("b+"));
        policy.matchesPattern("x", List.of("c+"));

        assertEquals(2, policy.cachedPatternCount());
    }

    @Test
    void globalPatternsMatchAfterInit() {
        var props = new LoggedProperties();
        props.setMaskPatterns(List.of("(?i)secret"));
        var policy = policy(props);
        assertTrue(policy.matchesPattern("my SECRET value", List.of()));
    }

    @Test
    void failOnInvalidGlobalPatternThrowsAtInit() {
        var props = new LoggedProperties();
        props.setMaskPatterns(List.of("[unclosed"));
        props.setFailOnInvalidMaskPatterns(true);
        assertThrows(IllegalArgumentException.class, () -> policy(props));
    }

    @Test
    void globalTypeMatching() {
        var props = new LoggedProperties();
        props.setMaskTypeNames(List.of("java.util.UUID"));
        var policy = policy(props);
        assertTrue(policy.matchesType(java.util.UUID.randomUUID(), null, List.of()));
        assertFalse(policy.matchesType("a string", String.class, List.of()));
    }

    @Test
    void annotationTypeMatching() {
        var policy = policy(new LoggedProperties());
        assertTrue(policy.matchesType(java.util.UUID.randomUUID(), null, List.of(java.util.UUID.class)));
    }
}
