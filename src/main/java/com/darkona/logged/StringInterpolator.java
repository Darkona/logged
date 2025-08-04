package com.darkona.logged;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringInterpolator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    public static String interpolate(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
