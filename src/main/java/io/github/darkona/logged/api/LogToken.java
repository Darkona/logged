package io.github.darkona.logged.api;

import java.util.Arrays;
import java.util.Optional;

public enum LogToken {

    METHOD_NAME("m"),
    METHOD_TYPE("t"),
    CLASS_NAME("c"),
    CLASS_LONG("C"),
    RETURN_VALUE("rV"),
    DURATION("d"),
    EXCEPTION_CLASS("ex"),
    EXCEPTION_MESSAGE("eM"),
    EXCEPTION_ORIGIN_CLASS("ec"),
    EXCEPTION_ORIGIN_METHOD("em"),
    ARGUMENTS("a"),
    RETURN_CLASS("rC"),
    LINE("L"),
    FILENAME("f"),
    ENTRY_ICON("eI"),
    EXIT_ICON("xI"),
    THROW_ICON("tI"),
    DEPTH_ICON("dI"),
    NULL("null"),
    DEPTH("h");


    private final String token;

    LogToken(String token) {
        this.token = token;
    }

    @SuppressWarnings("unused")
    public static Optional<LogToken> fromToken(String token) {
        return Arrays.stream(values())
                     .filter(v -> v.token.equals(token))
                     .findFirst();
    }

    public String token() {
        return token;
    }
}

