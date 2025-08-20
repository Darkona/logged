package io.github.darkona.logged.api;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.internals.LoggedEngine;
import io.github.darkona.logged.utils.StringInterpolator;

import java.util.HashMap;
import java.util.Map;

public record Arg(String className, String name, String value) {
    public String toString(String template, Logged.Values values) {
        Map<String, String> toks = new HashMap<>();
        toks.put(LogToken.CLASS_NAME.token(), className);
        toks.put("k", name);
        if (Logged.Values.ALL.equals(values)) {
            toks.put("v", value);
        } else if (Logged.Values.NULL.equals(values) && value == null) {
            toks.put("v", LoggedEngine.NULL);
        } else {
            toks.put("v", "");
        }
        return StringInterpolator.interpolate(template, toks);
    }
}
