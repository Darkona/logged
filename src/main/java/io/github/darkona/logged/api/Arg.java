package io.github.darkona.logged.api;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.internals.LoggedEngine;
import io.github.darkona.logged.utils.StringInterpolator;
import io.github.darkona.logged.utils.Transformer;

import java.util.HashMap;
import java.util.Map;

public record Arg(String className, String name, String value) {
    public String toString(String template, Logged.Values values, int truncate) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(LogToken.CLASS_NAME.token(), className);
        tokens.put("k", name);
        if (Logged.Values.ALL.equals(values)) {
            tokens.put("v", Transformer.truncate(value, truncate));
        } else if (Logged.Values.NULL.equals(values) && value == null) {
            tokens.put("v", LoggedEngine.NULL);
        } else {
            tokens.put("v", "");
        }
        return StringInterpolator.interpolate(template, tokens);
    }
}
