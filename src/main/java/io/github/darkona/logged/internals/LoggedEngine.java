package io.github.darkona.logged.internals;


import io.github.darkona.logged.Logged;
import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.Orange;
import io.github.darkona.logged.utils.Transformer;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class LoggedEngine {

    public static final String NULL = "null";
    private static final ThreadLocal<Deque<Data>> STACK = ThreadLocal.withInitial(ArrayDeque::new);
    private final LoggedProperties props;
    private final LogDecorator deco;
    private final List<LoggedPlugin> plugins;
    private List<Pattern> maskPatterns = List.of();
    private List<Class<?>> maskTypes = List.of();
    @Setter
    private boolean woven;


    public LoggedEngine(LoggedProperties props, LogDecorator deco, List<LoggedPlugin> plugins) {
        this.props = props;
        this.deco = deco;
        this.plugins = plugins;
        this.woven = props.isLogDepth();
    }

    private static void pop() {
        Deque<Data> s = STACK.get();
        if (!s.isEmpty()) s.pop();
        if (s.isEmpty()) STACK.remove();
    }

    @PostConstruct
    void init() {
        var log = LoggerFactory.getLogger(LoggedEngine.class);
        var msg = "@Logged engine initialized";
        if (props.isUseUtf8() && System.out.charset() != StandardCharsets.UTF_8) {
            Utf8Installer.install();
            msg += ", output UTF-8 enabled";
        }

        msg += woven ? ", and aspect weaving detected." : ".";

        log.info(deco.custom(Orange.DARK_ORANGE, msg));

        plugins.forEach(loggedPlugin -> {
            try {
                loggedPlugin.onLoad();
                if (props.isAnnounceLoad() && !loggedPlugin.announceLoad().isBlank()) log.info(loggedPlugin.announceLoad());
            } catch (Exception e) {
                var  m = "Error loading plugin: " + loggedPlugin.getClass().getSimpleName();
                log.error(m, e);
            }
        });

        // Pre-compile global mask patterns and resolve mask types once
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

    public Object logMethod(ProceedingJoinPoint pjp)
    throws Throwable {
        final var options = getLoggedOptions(pjp);
        final var data = assembleCallData(pjp, options);

        STACK.get().push(data);

        plugins.forEach(p -> p.onCall(pjp, data, options));

        try {
            final var result = pjp.proceed();
            afterSuccess(pjp, data, options, result);
            return result;
        } catch (Throwable ex) {
            afterFailure(pjp, data, options, ex);
            throw ex;
        } finally {
            pop();
            plugins.forEach(LoggedPlugin::afterMethod);
        }
    }

    private void afterSuccess(ProceedingJoinPoint pjp, Data data, Logged options, Object result) {
        putDuration(data);
        assembleReturnData(data, result, options);
        plugins.forEach(p -> p.onReturn(pjp, data, options));
    }

    private void afterFailure(ProceedingJoinPoint pjp, Data data, Logged options, Throwable ex) {
        putDuration(data);
        assembleExceptionData(ex, data);
        plugins.forEach(p -> p.onException(pjp, data, options, ex));
    }

    private void putDuration(Data data) {
        data.addToken(LogToken.DURATION, Long.toString(System.currentTimeMillis() - data.start()));
    }

    private StackTraceElement firstRelevantFrame(Throwable e) {
        return (e.getStackTrace() != null && e.getStackTrace().length > 0) ? e.getStackTrace()[0] :
               new StackTraceElement("unknown", "unknown", "unknown", -1);
    }

    private Logged getLoggedOptions(ProceedingJoinPoint pjp) {
        return ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(Logged.class) == null ?
               pjp.getTarget().getClass().getAnnotation(Logged.class) :
               ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(Logged.class);
    }

    @SuppressWarnings("unchecked")
    private Data assembleCallData(ProceedingJoinPoint pjp, Logged options) {

        var start = System.currentTimeMillis();
        var depth = STACK.get().size();
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
            String rawStr = Transformer.objectString(raw);

            boolean nameOrPos = masksByName.contains(names[i]) || maskIndexes.contains(i);
            boolean typeMatch = matchesType(raw, signature.getParameterTypes()[i], annoTypes, this.maskTypes);
            boolean patternMatch = matchesPattern(rawStr, annoPatterns, this.maskPatterns);

            String value;
            if (nameOrPos || typeMatch || patternMatch) {
                value = mask();
            } else {
                value = Transformer.truncate(rawStr, props.getMaxValueLength());
            }

            args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), names[i], value);
        }
        return new Data(map, args, start, depth, maskIndexes);
    }

    @SuppressWarnings("unchecked")
    private void assembleReturnData(Data data, Object o, Logged options) {
        data.addToken(LogToken.DURATION, String.valueOf(System.currentTimeMillis() - data.start()));
        data.addToken(LogToken.RETURN_CLASS, (o == null) ? NULL : o.getClass().getSimpleName());

        String raw = Transformer.objectString(o);

        boolean mustMask = props.isMaskReturn() || (options != null && options.maskReturn());
        if (!mustMask) {
            mustMask = matchesType(o, (o != null ? o.getClass() : null), List.of(), this.maskTypes)
                    || matchesPattern(raw, List.of(), this.maskPatterns);
        }

        String value;
        if (mustMask) {
            value = mask();
        } else {
            value = Transformer.truncate(raw, props.getMaxValueLength());
        }
        data.addToken(LogToken.RETURN_VALUE, value);
    }

    @SuppressWarnings("unchecked")
    private boolean matchesType(Object value, Class<?> declaredType, List<Class<?>>... sources) {
        Class<?> runtime = (value != null) ? value.getClass() : null;
        for (List<Class<?>> list : sources) {
            if (list == null) continue;
            for (Class<?> t : list) {
                if (t == null) continue;
                if (runtime != null && t.isAssignableFrom(runtime)) return true;
                if (declaredType != null && t.isAssignableFrom(declaredType)) return true;
            }
        }
        return false;
    }

    private boolean matchesPattern(String s, List<String> annoPatterns, List<Pattern> globalPatterns) {
        if (s == null) return false;
        if (annoPatterns != null) {
            for (String ap : annoPatterns) {
                if (ap == null || ap.isBlank()) continue;
                try {
                    if (Pattern.compile(ap).matcher(s).find()) return true;
                } catch (Throwable ignored) { /* ignore invalid patterns */ }
            }
        }
        if (globalPatterns != null) {
            for (Pattern p : globalPatterns) {
                if (p != null && p.matcher(s).find()) return true;
            }
        }
        return false;
    }

    private String mask() {
        return Transformer.truncate(Transformer.fill(props.getMaskString(), props.getMaskLength()), props.getMaskLength());
    }

    private void assembleExceptionData(Throwable e, Data data) {
        var origin = firstRelevantFrame(e);
        data.addToken(LogToken.EXCEPTION_CLASS, e.getClass().getSimpleName());
        data.addToken(LogToken.EXCEPTION_MESSAGE, e.getLocalizedMessage());
        data.addToken(LogToken.EXCEPTION_ORIGIN_CLASS, origin.getClassName());
        data.addToken(LogToken.EXCEPTION_ORIGIN_METHOD, origin.getMethodName());
        data.addToken(LogToken.LINE, String.valueOf(origin.getLineNumber()));
        data.addToken(LogToken.NULL, String.valueOf(origin.getFileName()));
        data.addToken(LogToken.FILENAME, origin.getFileName());
    }

}
