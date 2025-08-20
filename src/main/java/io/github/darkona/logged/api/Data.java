package io.github.darkona.logged.api;

import java.util.HashMap;
import java.util.Map;

public record Data(Map<LogToken, String> tokens, Arg[] args, Long start) {

    private static final Map<String, String> sTokens = new HashMap<>();

    public Data(Map<LogToken, String> tokens, Arg[] args, Long start) {
        this.tokens = tokens;
        this.args = args;
        this.start = start;
        tokens.forEach((key, value) -> sTokens.put(key.token(), value));
    }

    public void addToken(LogToken token, String value) {
        tokens.put(token, value);
        sTokens.put(token.token(), value);
    }

    public Map<String, String> tok() {
        return sTokens;
    }
}
