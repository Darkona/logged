package com.darkona.logged;


import com.darkona.logged.annotation.Logged;
import com.darkona.logged.utils.LogStrings;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
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
    private static final String RETURN_CLASS = "rC";
    private static final String LINE = "L";
    private static final String NULL = "null";

    public LoggedAspect() {
    }

    private static void assembleExceptionData(Throwable e, Data data, StackTraceElement origin) {
        data.map.put(EXCEPTION_CLASS, e.getClass().getName());
        data.map.put(EXCEPTION_MESSAGE, e.getLocalizedMessage());
        data.map.put(EXCEPTION_ORIGIN_CLASS, origin.getClassName());
        data.map.put(EXCEPTION_ORIGIN_METHOD, origin.getMethodName());
        data.map.put(LINE, String.valueOf(origin.getLineNumber()));
    }

    @PostConstruct
    void init() {
        LoggerFactory.getLogger(LoggedAspect.class).info(LogStrings.custom(156, 123, 23, "@Logged initialized."));
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

    void logCall(Logger log, Level level, Data data, Logged options) {

        if (options.callMsg() != null && !options.callMsg().isEmpty()) {

            log.atLevel(level).log(applyPattern(data, options.callMsg()));

        } else if (options.onCall()) {
            StringBuilder sb = new StringBuilder(String.format("→○ %s::%s called", data.map.get(CLASS_NAME), data.map.get(METHOD_NAME)));

            if (options.args()) {
                sb.append(" with args: [")
                  .append(makePrintableArgs(data.args, options.argValues()))
                  .append("]");
            } else {
                sb.append(".");
            }
            log.atLevel(level).log(sb.toString());
        }
    }

    void logExit(Logger log, Level level, Data data, Logged options) {
        if (options.onReturn() && options.returnMsg().isEmpty()) {
            StringBuilder sb = new StringBuilder(String.format("←○ %s::%s returned", data.map.get(CLASS_NAME), data.map.get(METHOD_NAME)));

            if (options.returnValue().equals(Logged.Values.ALL)) {
                sb.append(String.format(" with value: [(%s) %s]", data.map.get(RETURN_CLASS), objectString(data.map.get(RETURN_VALUE))));
            }

            if (options.time()) {
                sb.append(String.format(" Time taken: %s%s", data.map.get(DURATION), "ms"));
            }

            log.atLevel(level).log(sb.toString());
        } else if (!options.returnMsg().isEmpty()) {
            log.atLevel(level).log(applyPattern(data, options.returnMsg()));
        }
    }

    void logException(Logger log, Throwable e, Data data, Logged options) {
        var origin = e.getStackTrace()[0];

        if (options.onException() && options.exceptionMsg().isEmpty()) {
            StringBuilder sb = new StringBuilder(String.format("↑x %s::%s threw a %s: %s \n\tat %s.%s (%s:%d)",
                    data.map.get(CLASS_NAME),
                    data.map.get(METHOD_NAME),
                    e.getClass().getName(),
                    e.getLocalizedMessage(),
                    origin.getClassName(),
                    origin.getMethodName(),
                    origin.getFileName(),
                    origin.getLineNumber()
            ));

            if (options.time()) {
                sb.append(String.format(" Time taken: %s%s", data.map.get(DURATION), "ms"));
            }
            if (options.logStackTrace()) {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(sb.toString(), e);
            } else {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(sb.toString());
            }
        } else if (!options.exceptionMsg().isEmpty()) {

            assembleExceptionData(e, data, origin);

            if (options.logStackTrace()) {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(applyPattern(data, options.exceptionMsg()), e);
            } else {
                log.atLevel(getLoggingLevel(options.exceptionLevel(), Level.ERROR)).log(applyPattern(data, options.exceptionMsg()));
            }
        }
    }

    private Data assembleCallData(ProceedingJoinPoint pjp) {

        var start = System.currentTimeMillis();

        Map<String, String> map = new HashMap<>();

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

    private void assembleReturnData(Data data, Object o) {
        data.map.put(DURATION, String.valueOf(System.currentTimeMillis() - data.start));
        data.map.put(RETURN_CLASS, (o == null) ? NULL : o.getClass().getSimpleName());
        data.map.put(RETURN_VALUE, objectString(o));
    }

    private Level getLoggingLevel(String level, Level defaultLevel) {
        try {
            return Level.valueOf(level);
        } catch (Exception ignored) {
            return defaultLevel;
        }
    }


    private String applyPattern(Data data, String s) {

        String[] tokenKeys = {"%c", "%t", "%m", "%d", "%r", "%f", "%x", "%p", "%j", "%y", "%l"};

        String[] tokenValues = {
                data.map.get(CLASS_NAME),
                data.map.get(METHOD_TYPE),
                data.map.get(METHOD_NAME),
                data.map.get(DURATION),
                data.map.get(RETURN_VALUE),
                data.map.get(RETURN_CLASS),
                objectString(data.map.get(EXCEPTION_CLASS)),
                objectString(data.map.get(EXCEPTION_MESSAGE)),
                objectString(data.map.get(EXCEPTION_ORIGIN_CLASS)),
                objectString(data.map.get(EXCEPTION_ORIGIN_METHOD)),
                objectString(data.map.get(LINE))
        };

        var initialLength = 3 * data.args.length + tokenKeys.length;

        String[] keys = new String[initialLength];
        String[] vals = new String[initialLength];
        int i = 0;
        for (; i < data.args.length; i++) {

            keys[i * 3] = "%k[" + i + "]";
            keys[i * 3 + 1] = "%n[" + i + "]";
            keys[i * 3 + 2] = "%a[" + i + "]";

            vals[i * 3] = data.args[i].className;
            vals[i * 3 + 1] = data.args[i].name;
            vals[i * 3 + 2] = data.args[i].value;

            if (i * 3 + 2 == initialLength - tokenKeys.length - 1) {
                break;
            }
        }
        i += 3;

        for (; i < initialLength; i++) {
            keys[i] = tokenKeys[i];
            vals[i] = tokenValues[i];
        }

        return StringUtils.replaceEach(s, keys, vals);
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
                    values == Logged.Values.NULL ? "= " + NULL : "");
        }

    }
}
