package com.darkona.logged;


import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Aspect
public class LoggedAspect {

    private static final String METHOD_NAME = "m";
    private static final String METHOD_TYPE = "t";
    private static final String CLASS_NAME = "c";
    private static final String RETURN_VALUE = "rV";
    private static final String DURATION = "d";
    private static final String EXCEPTION_CLASS = "ex";
    private static final String EXCEPTION_MESSAGE = "eM";
    private static final String EXCEPTION_ORIGIN_CLASS = "ec";
    private static final String EXCEPTION_ORIGIN_METHOD = "em";
    private static final String ARGUMENTS = "a";
    private static final String RETURN_CLASS = "rC";
    private static final String LINE = "L";
    private static final String NULL = "null";
    private static final String FILENAME = "f";
    private static final String ENTRY_ICON = "eI";
    private static final String EXIT_ICON = "xI";
    private static final String THROW_ICON = "tI";
    private static final String ENTRY = "→○";
    private static final String EXIT = "←○";
    private static final String THROW = "↑x";
    private static final String ENTRY2 = "↓○";
    private final LoggedProperties loggedProperties;
    private final LogDecorator logDecorator;

    public LoggedAspect(LoggedProperties loggedProperties, LogDecorator logDecorator) {
        this.loggedProperties = loggedProperties;

        this.logDecorator = logDecorator;
    }

    private static void assembleExceptionData(Throwable e, Data data, StackTraceElement origin) {
        data.map.put(EXCEPTION_CLASS, e.getClass().getName());
        data.map.put(EXCEPTION_MESSAGE, e.getLocalizedMessage());
        data.map.put(EXCEPTION_ORIGIN_CLASS, origin.getClassName());
        data.map.put(EXCEPTION_ORIGIN_METHOD, origin.getMethodName());
        data.map.put(LINE, String.valueOf(origin.getLineNumber()));
        data.map.put(NULL, String.valueOf(origin.getFileName()));
        data.map.put(FILENAME, origin.getFileName());
    }

    @PostConstruct
    void init() {
        LoggerFactory.getLogger(LoggedAspect.class).info(logDecorator.custom(156, 123, 23, "@Logged initialized."));
    }

    @Around("@annotation(options)")
    public Object logMethod(ProceedingJoinPoint pjp, Logged options)
    throws Throwable {

        Data data = assembleCallData(pjp);

        Level level = getLoggingLevel(options.level(), Level.INFO);

        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());

        logCall(log, level, data, options);

        try {

            var o = pjp.proceed();

            assembleReturnData(data, o);

            logExit(log, level, data, options);

            return o;

        } catch (Throwable e) {

            data.map.put(DURATION, String.valueOf(System.currentTimeMillis() - data.start));

            logException(log, e, data, options);

            throw e;
        }
    }

    private Data assembleCallData(ProceedingJoinPoint pjp) {

        var start = System.currentTimeMillis();

        Map<String, String> map = new HashMap<>();
        map.put(ENTRY_ICON, loggedProperties.getIcons() ? logDecorator.blue(loggedProperties.getEntryIcon()) + " " : "");
        map.put(EXIT_ICON, loggedProperties.getIcons() ? logDecorator.blue(loggedProperties.getExitIcon()) + " " : "");
        map.put(THROW_ICON, loggedProperties.getIcons() ? logDecorator.blue(loggedProperties.getThrowIcon()) + " " : "");

        map.put(CLASS_NAME, pjp.getSignature().getDeclaringType().getSimpleName());
        map.put(METHOD_NAME, pjp.getSignature().getName());
        map.put(METHOD_TYPE, pjp.getSignature().toLongString());

        var signature = (MethodSignature) pjp.getSignature();

        Arg[] args = new Arg[signature.getParameterNames().length];

        for (int i = 0; i < signature.getParameterNames().length; i++) {
            args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), signature.getParameterNames()[i], objectString(pjp.getArgs()[i]));
        }

        return new Data(map, args, start);
    }

    void logCall(Logger log, Level level, Data data, Logged options) {

        String template;
        data.map.put(ARGUMENTS, makePrintableArgs(data.args, options.argValues()));

        if (options.callMsg() != null && !options.callMsg().isEmpty()) {
            template = options.callMsg();
            log.atLevel(level).log(StringInterpolator.interpolate(template, data.map));
        } else if (options.onCall()) {

            if (options.args()) {
                template = loggedProperties.getCallMsgArgs();
            } else {
                template = loggedProperties.getCallMsgNoArgs();
            }
            log.atLevel(level).log(StringInterpolator.interpolate(template, data.map));
        }

    }

    private void assembleReturnData(Data data, Object o) {
        data.map.put(DURATION, String.valueOf(System.currentTimeMillis() - data.start));
        data.map.put(RETURN_CLASS, (o == null) ? NULL : o.getClass().getSimpleName());
        data.map.put(RETURN_VALUE, objectString(o));
    }

    void logExit(Logger log, Level level, Data data, Logged options) {

        String template;
        if (options.onReturn() && options.returnMsg().isEmpty()) {
            template = loggedProperties.getExitMsg();

            if (options.returnValue().equals(Logged.Values.ALL)) {
                template = loggedProperties.getExitMsgValue();
            }

            if (options.time()) {
                template += " " + loggedProperties.getTimeTakenMsg();
            }

            log.atLevel(level).log(StringInterpolator.interpolate(template, data.map));
        } else if (!options.returnMsg().isEmpty()) {
            log.atLevel(level).log(StringInterpolator.interpolate(options.returnMsg(), data.map));
        }
    }

    void logException(Logger log, Throwable e, Data data, Logged options) {
        var origin = e.getStackTrace()[0];
        assembleExceptionData(e, data, origin);
        String template;
        if (options.onException() && options.exceptionMsg().isEmpty()) {
            template = loggedProperties.getThrowMsg();
            if (options.time()) {
                template += " " + loggedProperties.getTimeTakenMsg();
            }
            log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log();
            if (options.logStackTrace()) {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(StringInterpolator.interpolate(template, data.map), e);
            } else {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(StringInterpolator.interpolate(template, data.map));
            }
        } else if (!options.exceptionMsg().isEmpty()) {
            template = options.exceptionMsg();
            if (options.logStackTrace()) {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(StringInterpolator.interpolate(template, data.map), e);
            } else {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(StringInterpolator.interpolate(template, data.map));
            }
        }
    }

    private Level getLoggingLevel(String level, Level defaultLevel) {
        try {
            return Level.valueOf(level);
        } catch (Exception ignored) {
            return defaultLevel;
        }
    }

    private String objectString(Object o) {
        return o == null ? "null" : o.toString();
    }

    private String makePrintableArgs(Arg[] args, Logged.Values argValues) {
        return Arrays.stream(args)
                     .map(a -> a.toString(argValues))
                     .collect(Collectors.joining(","));
    }

    private record Data(Map<String, String> map, Arg[] args, Long start) {
    }

    private record Arg(String className, String name, String value) {

        public String toString(Logged.Values values) {
            return String.format("(%s) \"%s\"%s", className, name,
                    values == Logged.Values.ALL ? "= " + value :
                    values == Logged.Values.NULL && value == null ? "= " + NULL : "");
        }

    }
}
