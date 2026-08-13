package io.github.darkona.logged.api;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Data {

    private final Map<LogToken, String> tokens;
    private final Arg[] args;
    private final long start;
    private final int depth;
    @Getter
    private final Set<Integer> redactedPos;
    private final Map<String, String> stokens;

    public Data(Map<LogToken, String> tokens, Arg[] args, long start, int depth, Set<Integer> redactedPos) {
        this.tokens = new HashMap<>();
        if (tokens != null) {
            for (var e : tokens.entrySet()) {
                this.tokens.put(e.getKey(), nz(e.getValue()));
            }
        }
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
        Map<String, String> synced = new HashMap<>(tokens.size());
        for (var e : tokens.entrySet()) {
            synced.put(e.getKey().token(), nz(e.getValue()));
        }
        return synced;
    }

    // Null values normalize to "" so templates render cleanly instead of "null"
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /**
     * Add or replace a token value (per-instance). Null values are stored as "".
     */
    public void addToken(LogToken token, String value) {
        tokens.put(token, nz(value));
        stokens.put(token.token(), nz(value));
    }

    /**
     * Add a token with a custom key, as a String. Null values are stored as "".
     *
     * @param key   String key
     * @param value String value
     */
    public void addFlexToken(String key, String value) {
        stokens.put(key, nz(value));
    }

    /**
     * Get a token value (by String key)
     *
     * @param key String key
     * @return the token value, or "" if absent
     */
    public String get(String key) {
        var v = stokens.get(key);
        return v == null ? "" : v;
    }

    /**
     * Get a token value (by LogToken key).
     */
    public String get(LogToken token) {
        var v = tokens.get(token);
        return v == null ? "" : v;
    }

    /**
     * Returns a String-keyed view of this instance's tokens,
     * mapping {@code token.token()} → value. This is per-instance,
     * NOT global. The returned map is unmodifiable; use
     * {@link #addToken} or {@link #addFlexToken} to change tokens.
     */
    public Map<String, String> tok() {
        return Collections.unmodifiableMap(stokens);
    }

    public List<String> argNames() {
        if (args.length == 0) {return Collections.emptyList();}
        return Arrays.stream(args).map(Arg::name).toList();
    }

}
