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
import jakarta.annotation.PostConstruct;
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
    private List<Pattern> redactPatterns = List.of();
    private List<Class<?>> redactTypes = List.of();


    public LoggedEngine(LoggedProperties props, LogDecorator deco, List<LoggedPlugin> plugins) {
        this.props = props;
        this.deco = deco;
        this.plugins = plugins;
    }

    private static void pop() {
        Deque<Data> s = STACK.get();
        if (!s.isEmpty()) s.pop();
        if (s.isEmpty()) STACK.remove();
    }

    @PostConstruct
    void init() {
        var log = LoggerFactory.getLogger(LoggedEngine.class);

        if (props.isUseUtf8() && System.out.charset() != StandardCharsets.UTF_8) {
            Utf8Installer.install();
            log.info(deco.custom(Orange.DARK_ORANGE, "@Logged engine initialized with output UTF-8 enabled."));
        } else {
            log.info(deco.custom(Orange.DARK_ORANGE, "@Logged engine initialized."));
        }

        plugins.forEach(loggedPlugin -> {
            try {
                loggedPlugin.onLoad();
                if (props.isAnnounceLoad() && !loggedPlugin.announceLoad().isBlank()) log.info(loggedPlugin.announceLoad());
            } catch (Exception e) {
                var msg = "Error loading plugin: " + loggedPlugin.getClass().getSimpleName();
                log.error(msg, e);
            }
        });

        // Pre-compile global redaction patterns and resolve redaction types once
        if (props.getRedactPatterns() != null && !props.getRedactPatterns().isEmpty()) {
            List<Pattern> compiled = new ArrayList<>();
            for (String p : props.getRedactPatterns()) {
                if (p == null || p.isBlank()) continue;
                try {
                    compiled.add(Pattern.compile(p));
                } catch (Exception ex) {
                    if (props.isFailOnInvalidRedactPatterns()) {
                        throw new IllegalArgumentException("Invalid redact pattern: " + p, ex);
                    }
                    log.warn("Ignoring invalid redact pattern '{}': {}", p, ex.getMessage());
                }
            }
            this.redactPatterns = List.copyOf(compiled);
        }

        if (props.getRedactTypeNames() != null && !props.getRedactTypeNames().isEmpty()) {
            List<Class<?>> resolved = new ArrayList<>();
            for (String cn : props.getRedactTypeNames()) {
                if (cn == null || cn.isBlank()) continue;
                try {
                    resolved.add(Class.forName(cn));
                } catch (Throwable t) {
                    if (props.isFailOnUnresolvedRedactTypes()) {
                        throw new IllegalArgumentException("Could not resolve redact type: " + cn, t);
                    }
                    log.warn("Could not resolve redact type: {}", cn);
                }
            }
            this.redactTypes = List.copyOf(resolved);
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
        if (props.isUseIconTheme() && props.getIconTheme() != null) {
            map.put(LogToken.ENTRY_ICON, props.getIconTheme().entry());
            map.put(LogToken.THROW_ICON, props.getIconTheme().exception());
            map.put(LogToken.EXIT_ICON, props.getIconTheme().exit());
            map.put(LogToken.DEPTH_ICON, props.getIconTheme().depth());
        } else {
            map.put(LogToken.ENTRY_ICON, props.getEntryIcon());
            map.put(LogToken.THROW_ICON, props.getThrowIcon());
            map.put(LogToken.EXIT_ICON, props.getExitIcon());
            map.put(LogToken.DEPTH_ICON, props.getDepthIcon());
        }
        map.put(LogToken.CLASS_NAME, pjp.getSignature().getDeclaringType().getSimpleName());
        map.put(LogToken.CLASS_LONG, pjp.getSignature().getDeclaringType().getName());
        map.put(LogToken.METHOD_NAME, pjp.getSignature().getName());


        if (!options.args()) return new Data(map, new Arg[]{}, start, depth, Collections.emptySet());

        var signature = (MethodSignature) pjp.getSignature();
        map.put(LogToken.METHOD_TYPE, signature.getReturnType().getSimpleName());
        Arg[] args = signature.getParameterTypes() != null ? new Arg[signature.getParameterTypes().length] : new Arg[0];

        var names = ParameterNames.resolve(pjp);

        Set<String> redacts = new HashSet<>();
        Set<Integer> redactIndexes = new HashSet<>();

        if (options.redactArgValues().length > 0) {
            redacts.addAll(Arrays.asList(options.redactArgValues()));
        }
        if (options.redactAtPos().length > 0) {
            Arrays.stream(options.redactAtPos()).forEach(redactIndexes::add);
        }

        // Merge annotation-level redaction rules
        List<Class<?>> annoTypes = Arrays.asList(options.redactTypes());
        List<String> annoPatterns = Arrays.asList(options.redactPatterns());

        for (int i = 0; i < signature.getParameterTypes().length; i++) {
            Object raw = pjp.getArgs()[i];
            String rawStr = Transformer.objectString(raw);

            boolean nameOrPos = redacts.contains(names[i]) || redactIndexes.contains(i);
            boolean typeMatch = matchesType(raw, signature.getParameterTypes()[i], annoTypes, this.redactTypes);
            boolean patternMatch = matchesPattern(rawStr, annoPatterns, this.redactPatterns);

            String value;
            if (nameOrPos || typeMatch || patternMatch) {
                value = mask();
            } else {
                value = Transformer.truncate(rawStr, props.getMaxValueLength());
            }

            args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), names[i], value);
        }
        return new Data(map, args, start, depth, redactIndexes);
    }

    @SuppressWarnings("unchecked")
    private void assembleReturnData(Data data, Object o, Logged options) {
        data.addToken(LogToken.DURATION, String.valueOf(System.currentTimeMillis() - data.start()));
        data.addToken(LogToken.RETURN_CLASS, (o == null) ? NULL : o.getClass().getSimpleName());

        String raw = Transformer.objectString(o);

        boolean mustMask = props.isMaskReturn() || (options != null && options.maskReturn());
        if (!mustMask) {
            // Apply rule-based masking (type/pattern) to return values as well
            mustMask = matchesType(o, (o != null ? o.getClass() : null), List.of(), this.redactTypes)
                    || matchesPattern(raw, List.of(), this.redactPatterns);
        }

        String value = mustMask ? mask() : Transformer.truncate(raw, props.getMaxValueLength());
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
        return Transformer.truncate(Transformer.fill(props.getRedactMask(), props.getRedactLength()), props.getRedactLength());
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
