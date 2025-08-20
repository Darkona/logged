package io.github.darkona.logged.internals;


import io.github.darkona.logged.Logged;
import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.Yellow;
import io.github.darkona.logged.utils.Transformer;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoggedEngine {

    public static final String NULL = "null";
    private final LoggedProperties props;
    private final LogDecorator deco;
    private final List<LoggedPlugin> plugins;

    public LoggedEngine(LoggedProperties props, LogDecorator deco, List<LoggedPlugin> plugins) {
        this.props = props;
        this.deco = deco;
        this.plugins = plugins;
    }

    @PostConstruct
    void init() {
        LoggerFactory.getLogger(LoggedEngine.class).info(deco.custom(Yellow.GOLD, "@Logged engine initialized."));
    }

    public Object logMethod(ProceedingJoinPoint pjp)
    throws Throwable {
        Logged options = getLoggedOptions(pjp);
        Data data = assembleCallData(pjp, options);
        plugins.forEach(plugin -> plugin.onCall(pjp, data, options));

        try {
            var o = pjp.proceed();
            data.addToken(LogToken.DURATION, String.valueOf(System.currentTimeMillis() - data.start()));
            assembleReturnData(data, o);
            plugins.forEach(plugin -> plugin.onReturn(pjp, data, options));
            return o;

        } catch (Throwable e) {
            data.addToken(LogToken.DURATION, String.valueOf(System.currentTimeMillis() - data.start()));
            assembleExceptionData(e, data, e.getStackTrace()[0]);
            plugins.forEach(plugin -> plugin.onException(pjp, data, options, e));
            throw e;
        }
    }

    private static Logged getLoggedOptions(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        var ops = method.getAnnotation(Logged.class);
        if (ops == null) {
            ops = pjp.getTarget().getClass().getAnnotation(Logged.class);
        }
        return ops;
    }

    private Data assembleCallData(ProceedingJoinPoint pjp, Logged options) {

        var start = System.currentTimeMillis();

        Map<LogToken, String> map = new HashMap<>();
        map.put(LogToken.ENTRY_ICON, props.getIcons() ? deco.blue(props.getEntryIcon()) + " " : "");
        map.put(LogToken.EXIT_ICON, props.getIcons() ? deco.green(props.getExitIcon()) + " " : "");
        map.put(LogToken.THROW_ICON, props.getIcons() ? deco.red(props.getThrowIcon()) + " " : "");
        map.put(LogToken.CLASS_NAME, pjp.getSignature().getDeclaringType().getSimpleName());
        map.put(LogToken.CLASS_LONG, pjp.getSignature().getDeclaringType().getName());
        map.put(LogToken.METHOD_NAME, pjp.getSignature().getName());
        map.put(LogToken.METHOD_TYPE, pjp.getSignature().toLongString());

        if (options.args()) {
            var signature = (MethodSignature) pjp.getSignature();

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
            for (int i = 0; i < signature.getParameterTypes().length; i++) {
                var value = Transformer.objectString(pjp.getArgs()[i]);

                if (redacts.contains(names[i]) || redactIndexes.contains(i)) {
                    value = Transformer.mask(value, 0, props.getRedactMask()).substring(0, props.getRedactLength());
                }
                args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), names[i], value);
            }
            return new Data(map, args, start);
        }
        return new Data(map, new Arg[]{}, start);
    }

    private void assembleReturnData(Data data, Object o) {
        data.addToken(LogToken.DURATION, String.valueOf(System.currentTimeMillis() - data.start()));
        data.addToken(LogToken.RETURN_CLASS, (o == null) ? NULL : o.getClass().getSimpleName());
        data.addToken(LogToken.RETURN_VALUE, Transformer.objectString(o));
    }

    private static void assembleExceptionData(Throwable e, Data data, StackTraceElement origin) {
        data.addToken(LogToken.EXCEPTION_CLASS, e.getClass().getSimpleName());
        data.addToken(LogToken.EXCEPTION_MESSAGE, e.getLocalizedMessage());
        data.addToken(LogToken.EXCEPTION_ORIGIN_CLASS, origin.getClassName());
        data.addToken(LogToken.EXCEPTION_ORIGIN_METHOD, origin.getMethodName());
        data.addToken(LogToken.LINE, String.valueOf(origin.getLineNumber()));
        data.addToken(LogToken.NULL, String.valueOf(origin.getFileName()));
        data.addToken(LogToken.FILENAME, origin.getFileName());
    }
}
