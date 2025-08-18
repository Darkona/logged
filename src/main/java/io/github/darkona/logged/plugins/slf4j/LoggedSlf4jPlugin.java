package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.Arg;
import io.github.darkona.logged.Data;
import io.github.darkona.logged.Logged;
import io.github.darkona.logged.LoggedAspect;
import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.colors.Green;
import io.github.darkona.logged.internals.LogDecorator;
import io.github.darkona.logged.internals.LogToken;
import io.github.darkona.logged.plugins.LoggedPlugin;
import io.github.darkona.logged.strings.StringInterpolator;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.darkona.logged.LoggedAspect.NULL;

@Component
public class LoggedSlf4jPlugin implements LoggedPlugin {

    private final LoggedProperties props;
    private final LogDecorator deco;

    public LoggedSlf4jPlugin(LoggedProperties props, LogDecorator deco) {
        this.props = props;
        this.deco = deco;
    }

    @PostConstruct
    void init() {
        LoggerFactory.getLogger(LoggedAspect.class)
                     .info(deco.custom(Green.LAWN_GREEN, "@Logged-Slf4j Plugin initialized."));
    }

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        if (isEnabled(log, options.level())) {
            logCall(log, options.level(), data, options);
        }
    }

    void logCall(Logger log, Level level, Data data, Logged options) {

        String template = "";
        if (options.callMsg() != null && !options.callMsg().isEmpty()) {
            template = options.callMsg();
            log.atLevel(level).log(StringInterpolator.interpolate(template, data.tok()));
        } else if (options.onCall()) {

            if (options.args()) {
                template = props.getCallMsgArgs();
                data.addToken(LogToken.ARGUMENTS, makePrintableArgs(data.args(), options.argValues()));
            } else {
                template = props.getCallMsgNoArgs();
            }
            log.atLevel(level).log(StringInterpolator.interpolate(template, data.tok()));
        }
    }

    @Override
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        if (isEnabled(log, options.level())) {
            logReturn(log, options.level(), data, options);
        }
    }

    void logReturn(Logger log, Level level, Data data, Logged options) {

        String template;
        if (options.onReturn() && options.returnMsg().isEmpty()) {
            template = props.getExitMsg();

            if (options.returnValue().equals(Logged.Values.ALL)) {
                template = props.getExitMsgValue();
            } else if (options.returnValue().equals(Logged.Values.NULL)) {
                if (data.tokens().get(LogToken.RETURN_VALUE).equals(NULL)) {
                    template = props.getExitMsgValue();
                } else {
                    template = props.getExitMsg();
                }
            } else if (options.returnValue().equals(Logged.Values.NONE)) {
                template = "";
            }

            if (options.time()) {
                template += " " + props.getTimeTakenMsg();
            }

            log.atLevel(level).log(StringInterpolator.interpolate(template, data.tok()));
        } else if (!options.returnMsg().isEmpty()) {
            log.atLevel(level).log(StringInterpolator.interpolate(options.returnMsg(), data.tok()));
        }
    }

    @Override
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception) {
        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        if (isEnabled(log, options.exceptionLevel())) {
            logException(log, exception, data, options);
        }
    }

    void logException(Logger log, Throwable e, Data data, Logged options) {
        String template;
        if (options.onException() && options.exceptionMsg().isEmpty()) {
            template = props.getThrowMsg();
            if (options.time()) {
                template += " " + props.getTimeTakenMsg();
            }
            var message = StringInterpolator.interpolate(template, data.tok());
            if (options.logStackTrace()) {
                log.atLevel(options.exceptionLevel()).log(message, e);
            } else {
                log.atLevel(options.exceptionLevel()).log(message);
            }
        } else if (!options.exceptionMsg().isEmpty()) {
            template = options.exceptionMsg();
            var message = StringInterpolator.interpolate(template, data.tok());
            if (options.logStackTrace()) {
                log.atLevel(options.exceptionLevel()).log(message, e);
            } else {
                log.atLevel(options.exceptionLevel()).log(message);
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
                     .map(a -> a.toString(props.getArgsTemplate(), argValues))
                     .collect(Collectors.joining(","));
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
