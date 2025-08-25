package io.github.darkona.logged.api;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Data {

    private final Map<LogToken, String> tokens;
    private final Arg[] args;
    private final long start;
    private final int depth;
    @Getter
    private final Set<Integer> redactedPos;
    private final Map<String, String> stokens;

    public Data(Map<LogToken, String> tokens, Arg[] args, long start, int depth, Set<Integer> redactedPos) {
        this.tokens = tokens == null ? new HashMap<>() : new HashMap<>(tokens);
        this.args = args == null ? new Arg[0] : args;
        this.start = start;
        this.depth = depth;
        this.redactedPos = redactedPos;
        this.stokens = syncTokens();
    }

    public int depth() {
        return depth;
    }

    public long start() {
        return start;
    }

    public Arg[] args() {
        return args;
    }

    public Map<LogToken, String> tokens() {
        return tokens;
    }

    private Map<String, String> syncTokens() {
        return tokens.entrySet().stream()
                     .collect(Collectors.toMap(e -> e.getKey().token(),
                             Map.Entry::getValue, (a, b) -> b, () -> new HashMap<>(tokens.size())));
    }

    /**
     * Add or replace a token value (per-instance).
     */
    public void addToken(LogToken token, String value) {
        tokens.put(token, value);
        stokens.put(token.token(), value);
    }

    /**
     * Add a token with a custom key, as a String
     * @param key String key
     * @param value String value
     */
    public void addFlexToken(String key, String value) {
        stokens.put(key, value);
    }

    /**
     * Get a token value (by String key)
     * @param key String key
     * @return the token value
     */
    public String get(String key) {
        return stokens.get(key) == null ? "" : stokens.get(key);
    }

    /**
     * Get a token value (by LogToken key).
     */
    public String get(LogToken token) {
        return tokens.get(token) == null ? "" : tokens.get(token);
    }

    /**
     * Returns a String-keyed view of this instance's tokens,
     * mapping {@code token.token()} → value. This is per-instance,
     * NOT global. The returned map is a copy.
     */
    public Map<String, String> tok() {
        return stokens;
    }

    public List<String> argNames() {
        if (args.length == 0) {return Collections.emptyList();}
        return Arrays.stream(args).map(Arg::name).toList();
    }

}
