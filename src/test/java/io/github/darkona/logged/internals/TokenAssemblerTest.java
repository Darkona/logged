package io.github.darkona.logged.internals;

import io.github.darkona.logged.LoggedProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenAssemblerTest {

    private static TokenAssembler assembler(List<String> skipPrefixes) {
        var props = new LoggedProperties();
        if (skipPrefixes != null) props.getStack().setSkipPrefixes(skipPrefixes);
        return new TokenAssembler(props, new MaskingPolicy(props));
    }

    private static Throwable withTrace(StackTraceElement... frames) {
        var e = new RuntimeException("boom");
        e.setStackTrace(frames);
        return e;
    }

    private static StackTraceElement frame(String className) {
        return new StackTraceElement(className, "method", className + ".java", 42);
    }

    @Test
    void defaultPrefixesSkipJdkAndFrameworkFrames() {
        var e = withTrace(
                frame("java.util.ArrayList"),
                frame("org.springframework.aop.SomeProxy"),
                frame("io.github.darkona.logged.internals.LoggedEngine"),
                frame("com.example.app.Service"));

        var origin = assembler(null).firstRelevantFrame(e);
        assertEquals("com.example.app.Service", origin.getClassName());
    }

    @Test
    void customPrefixesReplaceDefaults() {
        var e = withTrace(
                frame("com.example.app.Service"),
                frame("java.util.ArrayList"));

        var origin = assembler(List.of("com.example.")).firstRelevantFrame(e);
        assertEquals("java.util.ArrayList", origin.getClassName());
    }

    @Test
    void emptyPrefixListKeepsFirstFrame() {
        var e = withTrace(
                frame("java.util.ArrayList"),
                frame("com.example.app.Service"));

        var origin = assembler(List.of()).firstRelevantFrame(e);
        assertEquals("java.util.ArrayList", origin.getClassName());
    }

    @Test
    void allFramesSkippedFallsBackToFirstFrame() {
        var e = withTrace(
                frame("java.util.ArrayList"),
                frame("jdk.internal.reflect.Whatever"));

        var origin = assembler(null).firstRelevantFrame(e);
        assertEquals("java.util.ArrayList", origin.getClassName());
    }

    @Test
    void emptyTraceFallsBackToUnknownFrame() {
        var origin = assembler(null).firstRelevantFrame(withTrace());
        assertEquals("unknown", origin.getClassName());
        assertEquals(-1, origin.getLineNumber());
    }
}
