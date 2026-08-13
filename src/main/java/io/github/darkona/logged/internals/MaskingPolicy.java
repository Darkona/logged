package io.github.darkona.logged.internals;

import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.utils.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Decides whether a value must be masked — by type, by regex pattern, or by
 * annotation-level rules — and produces the mask string. Global patterns and
 * types from configuration are compiled/resolved once in {@link #init()};
 * annotation-level patterns are compiled lazily and cached.
 */
public class MaskingPolicy {

    private static final Logger log = LoggerFactory.getLogger(MaskingPolicy.class);

    private final LoggedProperties props;
    private List<Pattern> maskPatterns = List.of();
    private List<Class<?>> maskTypes = List.of();
    // Bounded so dynamically generated annotation patterns (a documented
    // anti-pattern) cannot grow the cache without limit
    private final Map<String, Optional<Pattern>> annoPatternCache;

    MaskingPolicy(LoggedProperties props) {
        this.props = props;
        this.annoPatternCache = Collections.synchronizedMap(
                new BoundedLruMap<>(props.getCache().getMaxAnnotationPatterns()));
    }

    /** Pre-compiles global mask patterns and resolves mask types once. */
    void init() {
        if (props.getMaskPatterns() != null && !props.getMaskPatterns().isEmpty()) {
            List<Pattern> compiled = new ArrayList<>();
            for (String p : props.getMaskPatterns()) {
                if (p == null || p.isBlank()) continue;
                try {
                    compiled.add(Pattern.compile(p));
                } catch (Exception ex) {
                    if (props.isFailOnInvalidMaskPatterns()) {
                        throw new IllegalArgumentException("Invalid mask pattern: " + p, ex);
                    }
                    log.warn("Ignoring invalid mask pattern '{}': {}", p, ex.getMessage());
                }
            }
            this.maskPatterns = List.copyOf(compiled);
        }

        if (props.getMaskTypeNames() != null && !props.getMaskTypeNames().isEmpty()) {
            List<Class<?>> resolved = new ArrayList<>();
            for (String cn : props.getMaskTypeNames()) {
                if (cn == null || cn.isBlank()) continue;
                try {
                    resolved.add(Class.forName(cn));
                } catch (Throwable t) {
                    if (props.isFailOnUnresolvedMaskTypes()) {
                        throw new IllegalArgumentException("Could not resolve mask type: " + cn, t);
                    }
                    log.warn("Could not resolve mask type: {}", cn);
                }
            }
            this.maskTypes = List.copyOf(resolved);
        }
    }

    /** True when the value's runtime or declared type matches an annotation-level or global mask type. */
    boolean matchesType(Object value, Class<?> declaredType, List<Class<?>> annoTypes) {
        Class<?> runtime = (value != null) ? value.getClass() : null;
        return matchesTypeIn(annoTypes, runtime, declaredType) || matchesTypeIn(maskTypes, runtime, declaredType);
    }

    private static boolean matchesTypeIn(List<Class<?>> list, Class<?> runtime, Class<?> declaredType) {
        if (list == null) return false;
        for (Class<?> t : list) {
            if (t == null) continue;
            if (runtime != null && t.isAssignableFrom(runtime)) return true;
            if (declaredType != null && t.isAssignableFrom(declaredType)) return true;
        }
        return false;
    }

    /** True when the string matches an annotation-level or global mask pattern (substring semantics). */
    boolean matchesPattern(String s, List<String> annoPatterns) {
        if (s == null) return false;
        if (annoPatterns != null) {
            for (String ap : annoPatterns) {
                if (ap == null || ap.isBlank()) continue;
                var compiled = annoPatternCache.get(ap);
                if (compiled == null) {
                    // Compile outside the cache lock; racing threads may both
                    // compile the same pattern once, which is harmless
                    try {
                        compiled = Optional.of(Pattern.compile(ap));
                    } catch (Exception ex) {
                        // A typo'd pattern means the value the user believes masked is
                        // logged in cleartext, so it must be loudly visible
                        log.warn("@Logged ignoring invalid mask pattern '{}': {}", ap, ex.getMessage());
                        compiled = Optional.empty();
                    }
                    annoPatternCache.put(ap, compiled);
                }
                if (compiled.isPresent() && compiled.get().matcher(s).find()) return true;
            }
        }
        for (Pattern p : maskPatterns) {
            if (p != null && p.matcher(s).find()) return true;
        }
        return false;
    }

    int cachedPatternCount() {
        return annoPatternCache.size();
    }

    String mask() {
        return Transformer.truncate(Transformer.fill(props.getMaskString(), props.getMaskLength()), props.getMaskLength());
    }
}
