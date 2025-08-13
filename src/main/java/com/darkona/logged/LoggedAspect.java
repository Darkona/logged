package com.darkona.logged;


import com.darkona.logged.internals.LogDecorator;
import com.darkona.logged.internals.MdcContext;
import com.darkona.logged.internals.ParameterNames;
import com.darkona.logged.strings.StringInterpolator;
import com.darkona.logged.strings.Transformer;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        //System.out.println("🔥🔥🔥 LOGGED ASPECT WAS CALLED 🔥🔥🔥");
        Data data = assembleCallData(pjp, options);

        Level level = options.level();

        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());

        if (isEnabled(log, options.level())) {
            logCall(log, level, data, options);
        }


        try {

            var o = pjp.proceed();

            if (isEnabled(log, options.level())) {
                assembleReturnData(data, o);
                logExit(log, level, data, options);
            }

            return o;

        } catch (Throwable e) {

            data.map.put(DURATION, String.valueOf(System.currentTimeMillis() - data.start));

            if (isEnabled(log, options.exceptionLevel())) {
                logException(log, e, data, options);
            }

            throw e;
        }
    }

    private Data assembleCallData(ProceedingJoinPoint pjp, Logged options) {

        var start = System.currentTimeMillis();

        Map<String, String> map = new HashMap<>();
        map.put(ENTRY_ICON, loggedProperties.getIcons() ? logDecorator.blue(loggedProperties.getEntryIcon()) + " " : "");
        map.put(EXIT_ICON, loggedProperties.getIcons() ? logDecorator.green(loggedProperties.getExitIcon()) + " " : "");
        map.put(THROW_ICON, loggedProperties.getIcons() ? logDecorator.red(loggedProperties.getThrowIcon()) + " " : "");

        map.put(CLASS_NAME, pjp.getSignature().getDeclaringType().getSimpleName());
        map.put(METHOD_NAME, pjp.getSignature().getName());
        map.put(METHOD_TYPE, pjp.getSignature().toLongString());

        if (options.args()) {
            var signature = (MethodSignature) pjp.getSignature();

            Arg[] args = signature.getParameterTypes() != null ? new Arg[signature.getParameterTypes().length] : new Arg[0];
            var names = ParameterNames.resolve(pjp);

            Set<String> redacts = new HashSet<>();
            Set<Integer> redactIndexes = new HashSet<>();

            if (options.redactArgValues().length > 0) {
                redacts.addAll(Arrays.asList(options.redactArgValues()));
            }
            if(options.redactAtPos().length > 0){
                Arrays.stream(options.redactAtPos()).forEach(redactIndexes::add);
            }
            for (int i = 0; i < signature.getParameterTypes().length; i++) {
                var value = objectString(pjp.getArgs()[i]);
                if (redacts.contains(names[i]) || redactIndexes.contains(i)) {
                    value = Transformer.mask(value, 0, '█').substring(0,5);
                }

                args[i] = new Arg(signature.getParameterTypes()[i].getSimpleName(), names[i], value);
            }
            return new Data(map, args, start);
        }
        return new Data(map, new Arg[]{}, start);
    }

    void logCall(Logger log, Level level, Data data, Logged options) {

        String template = "";
        if (options.withMDC()) {
            template += MdcContext.asPrefix();
        }
        if (options.callMsg() != null && !options.callMsg().isEmpty()) {
            template = options.callMsg();
            log.atLevel(level).log(StringInterpolator.interpolate(template, data.map));
        } else if (options.onCall()) {

            if (options.args()) {
                data.map.put(ARGUMENTS, makePrintableArgs(data.args, options.argValues()));
                if (options.argValues().equals(Logged.Values.ALL)) {
                    template = loggedProperties.getCallMsgArgs();
                } else if (options.argValues().equals(Logged.Values.NONE)) {
                    template = "";
                } else if (options.argValues().equals(Logged.Values.NULL)) {
                    if (Arrays.stream(data.args).anyMatch(arg -> arg.value.equals(NULL))) {

                        var args2 = Arrays.stream(data.args).filter(arg -> arg.value.equals(NULL)).toList();
                        Data nulldata = new Data(data.map, args2.toArray(new Arg[]{}), data.start);
                        template = loggedProperties.getCallMsgArgs();
                        log.atLevel(level).log(StringInterpolator.interpolate(template, nulldata.map));
                        return;

                    } else {
                        template = loggedProperties.getCallMsgNoArgs();
                    }
                }

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
            } else if (options.returnValue().equals(Logged.Values.NULL)) {
                if (data.map.get(RETURN_VALUE).equals(NULL)) {
                    template = loggedProperties.getExitMsgValue();
                } else {
                    template = loggedProperties.getExitMsg();
                }
            } else if (options.returnValue().equals(Logged.Values.NONE)) {
                template = "";
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
            if (options.logStackTrace()) {
                log.atLevel(options.exceptionLevel()).log(StringInterpolator.interpolate(template, data.map), e);
            } else {
                log.atLevel(options.exceptionLevel()).log(StringInterpolator.interpolate(template, data.map));
            }
        } else if (!options.exceptionMsg().isEmpty()) {
            template = options.exceptionMsg();
            if (options.logStackTrace()) {
                log.atLevel(options.exceptionLevel()).log(StringInterpolator.interpolate(template, data.map), e);
            } else {
                log.atLevel(options.exceptionLevel()).log(StringInterpolator.interpolate(template, data.map));
            }
        }
    }

    private String objectString(Object o) {
        try {
            return o == null ? "null" : o.toString();
        } catch (Throwable t) {
            return "toString Error: " + o.getClass().getSimpleName();
        }
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

    private boolean isEnabled(Logger log, Level lvl) {
        return switch (lvl) {
            case TRACE -> log.isTraceEnabled();
            case DEBUG -> log.isDebugEnabled();
            case INFO -> log.isInfoEnabled();
            case WARN -> log.isWarnEnabled();
            case ERROR -> log.isErrorEnabled();
        };
    }
}
