package io.github.darkona.logged.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Data(Map<LogToken, String> tokens, Arg[] args, Long start, Integer depth, Set<Integer> redactedPos) {

    public Data {
        if (tokens == null) {
            tokens = new HashMap<>();
        }
        if (args == null) {
            args = new Arg[0];
        }
        if (start == null) {
            start = System.currentTimeMillis();
        }
        if (depth == null) {
            depth = 0;
        }
    }

    /**
     * Add or replace a token value (per-instance).
     */
    public void addToken(LogToken token, String value) {
        tokens.put(token, value);
    }

    /**
     * Get a token value (by enum key).
     */
    public String get(LogToken token) {
        var tok = tokens.get(token);
        return tok == null ? "" : tok;
    }

    /**
     * Returns a String-keyed view of this instance's tokens,
     * mapping {@code token.token()} → value. This is per-instance,
     * NOT global. The returned map is a copy.
     */
    public Map<String, String> tok() {
        return tokens.entrySet().stream()
                     .collect(Collectors.toMap(e -> e.getKey().token(),
                             Map.Entry::getValue, (a, b) -> b, () -> new HashMap<>(tokens.size())));
    }

    public List<String> argNames() {
        if (args.length == 0) {return Collections.emptyList();}
        return Arrays.stream(args).map(Arg::name).toList();
    }

}
