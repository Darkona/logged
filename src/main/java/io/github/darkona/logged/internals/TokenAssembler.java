package io.github.darkona.logged.internals;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.utils.Transformer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.darkona.logged.internals.LoggedEngine.NULL;

/**
 * Builds the {@link Data} token maps for each phase of an intercepted call:
 * entry (class/method/icons/arguments), return (class/value), exception
 * (class/message/origin frame) and duration. Masking decisions are delegated
 * to {@link MaskingPolicy}.
 */
public class TokenAssembler {

    private final LoggedProperties props;
    private final MaskingPolicy masking;
    private final List<String> stackSkipPrefixes;

    TokenAssembler(LoggedProperties props, MaskingPolicy masking) {
        this.props = props;
        this.masking = masking;
        var prefixes = props.getStack().getSkipPrefixes();
        this.stackSkipPrefixes = prefixes == null ? List.of() : List.copyOf(prefixes);
    }

    Data assembleCallData(ProceedingJoinPoint pjp, Logged options, int depth) {

        var start = System.nanoTime();
        Map<LogToken, String> map = new HashMap<>();
        boolean themed = (props.isUseIconTheme() && props.getIconTheme() != null);

        map.put(LogToken.CALL_ICON, themed ? props.getIconTheme().entry() : props.getCallIcon());
        map.put(LogToken.EXCEPTION_ICON, themed ? props.getIconTheme().exception() : props.getExceptionIcon());
        map.put(LogToken.RETURN_ICON, themed ? props.getIconTheme().exit() : props.getReturnIcon());
        map.put(LogToken.DEPTH_ICON, themed ? props.getIconTheme().depth() : props.getDepthIcon());

        map.put(LogToken.CLASS_NAME, pjp.getSignature().getDeclaringType().getSimpleName());
        map.put(LogToken.CLASS_LONG, pjp.getSignature().getDeclaringType().getName());
        map.put(LogToken.METHOD_NAME, pjp.getSignature().getName());


        if (!options.args()) return new Data(map, new Arg[]{}, start, depth, Collections.emptySet());

        var signature = (MethodSignature) pjp.getSignature();
        map.put(LogToken.METHOD_TYPE, signature.getReturnType().getSimpleName());
        Arg[] args = signature.getParameterTypes() != null ? new Arg[signature.getParameterTypes().length] : new Arg[0];

        var names = ParameterNames.resolve(pjp);

        Set<String> masksByName = new HashSet<>();
        Set<Integer> maskIndexes = new HashSet<>();

        if (options.maskArgValues().length > 0) {
            masksByName.addAll(Arrays.asList(options.maskArgValues()));
        }
        if (options.maskAtPos().length > 0) {
            Arrays.stream(options.maskAtPos()).forEach(maskIndexes::add);
        }

        // Merge annotation-level masking rules
        List<Class<?>> annoTypes = Arrays.asList(options.maskTypes());
        List<String> annoPatterns = Arrays.asList(options.maskPatterns());

        for (int i = 0; i < signature.getParameterTypes().length; i++) {
            Object raw = pjp.getArgs()[i];

            // Values.NONE never prints values: skip the (possibly deep) stringification
            if (options.argValues() == Logged.Values.NONE) {
                args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), names[i], "");
                continue;
            }

            String rawStr = Transformer.objectString(raw);

            boolean nameOrPos = masksByName.contains(names[i]) || maskIndexes.contains(i);
            boolean typeMatch = masking.matchesType(raw, signature.getParameterTypes()[i], annoTypes);
            boolean patternMatch = masking.matchesPattern(rawStr, annoPatterns);

            String value;
            if (nameOrPos || typeMatch || patternMatch) {
                value = masking.mask();
            } else {
                value = Transformer.truncate(rawStr, props.getMaxValueLength());
            }

            args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), names[i], value);
        }
        return new Data(map, args, start, depth, maskIndexes);
    }

    void assembleReturnData(Data data, Object o, Logged options) {
        data.addToken(LogToken.RETURN_CLASS, (o == null) ? NULL : o.getClass().getSimpleName());

        if (options != null && options.returnValue() == Logged.Values.NONE) {
            data.addToken(LogToken.RETURN_VALUE, "");
            return;
        }

        String raw = Transformer.objectString(o);

        boolean mustMask = props.isMaskReturn() || (options != null && options.maskReturn());
        if (!mustMask) {
            mustMask = masking.matchesType(o, (o != null ? o.getClass() : null), List.of())
                    || masking.matchesPattern(raw, List.of());
        }

        String value;
        if (mustMask) {
            value = masking.mask();
        } else {
            value = Transformer.truncate(raw, props.getMaxValueLength());
        }
        data.addToken(LogToken.RETURN_VALUE, value);
    }

    void assembleExceptionData(Throwable e, Data data) {
        var origin = firstRelevantFrame(e);
        data.addToken(LogToken.EXCEPTION_CLASS, e.getClass().getSimpleName());
        data.addToken(LogToken.EXCEPTION_MESSAGE, e.getLocalizedMessage());
        data.addToken(LogToken.EXCEPTION_ORIGIN_CLASS, origin.getClassName());
        data.addToken(LogToken.EXCEPTION_ORIGIN_METHOD, origin.getMethodName());
        data.addToken(LogToken.LINE, String.valueOf(origin.getLineNumber()));
        data.addToken(LogToken.FILENAME, origin.getFileName());
    }

    void putDuration(Data data) {
        data.addToken(LogToken.DURATION, Long.toString((System.nanoTime() - data.start()) / 1_000_000));
    }

    /**
     * First frame that belongs to application code, so EXCEPTION_ORIGIN_* tokens
     * point at the user's class instead of JDK or framework internals.
     * The skipped prefixes come from {@code logged.stack.skip-prefixes}.
     */
    StackTraceElement firstRelevantFrame(Throwable e) {
        var trace = e.getStackTrace();
        if (trace == null || trace.length == 0) {
            return new StackTraceElement("unknown", "unknown", "unknown", -1);
        }
        for (StackTraceElement frame : trace) {
            if (!skipsFrame(frame.getClassName())) {
                return frame;
            }
        }
        return trace[0];
    }

    private boolean skipsFrame(String className) {
        for (String prefix : stackSkipPrefixes) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }
}
