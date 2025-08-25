package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.Green;
import io.github.darkona.logged.utils.StringInterpolator;
import io.github.darkona.logged.utils.Transformer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.darkona.logged.internals.LoggedEngine.NULL;


/**
 * SLF4J logging plugin for {@code @Logged} that formats and emits call/return/exception
 * messages using {@link org.slf4j.Logger} and the library's token interpolation.
 *
 * <p><strong>Behavior</strong>
 * <ul>
 *   <li><b>onCall</b>:
 *     <ul>
 *       <li>Computes a visual {@code depth} marker (e.g. {@code >>>}) based on {@link io.github.darkona.logged.api.Data#depth()}
 *           and stores it as {@link io.github.darkona.logged.api.LogToken#DEPTH}.</li>
 *       <li>If {@link io.github.darkona.logged.Logged#callMsg()} is non-empty, logs that template regardless of
 *           {@link io.github.darkona.logged.Logged#onCall()}.</li>
 *       <li>Otherwise, if {@code onCall==true}, selects a default template from
 *           {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getCallMsgArgs()} or
 *           {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getCallMsgNoArgs()} depending on
 *           {@link io.github.darkona.logged.Logged#args()} and adds an {@link io.github.darkona.logged.api.LogToken#ARGUMENTS}
 *           token built from {@link io.github.darkona.logged.api.Arg#toString(String, io.github.darkona.logged.Logged.Values)}.</li>
 *     </ul>
 *   </li>
 *   <li><b>onReturn</b>:
 *     <ul>
 *       <li>If {@link io.github.darkona.logged.Logged#returnMsg()} is non-empty, logs that template regardless of
 *           {@link io.github.darkona.logged.Logged#onReturn()}.</li>
 *       <li>Otherwise, if {@code onReturn==true}, selects a default exit template:
 *         <ul>
 *           <li>{@link io.github.darkona.logged.Logged.Values#ALL} → {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getExitMsgValue()}</li>
 *           <li>{@link io.github.darkona.logged.Logged.Values#NULL} → uses {@code getExitMsgValue()} only when
 *               {@link io.github.darkona.logged.api.LogToken#RETURN_VALUE} equals
 *               {@link io.github.darkona.logged.internals.LoggedEngine#NULL}; otherwise {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getExitMsg()}.</li>
 *           <li>{@link io.github.darkona.logged.Logged.Values#NONE} → no exit message (empty template).</li>
 *         </ul>
 *       </li>
 *       <li>When {@link io.github.darkona.logged.Logged#time()} is true, appends
 *           {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getTimeTakenMsg()} to the chosen template.</li>
 *     </ul>
 *   </li>
 *   <li><b>onException</b>:
 *     <ul>
 *       <li>If {@link io.github.darkona.logged.Logged#exceptionMsg()} is non-empty, logs that template regardless of
 *           {@link io.github.darkona.logged.Logged#onException()}.</li>
 *       <li>Otherwise, if {@code onException==true}, uses {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getThrowMsg()} and,
 *           when {@link io.github.darkona.logged.Logged#time()} is true, appends {@code getTimeTakenMsg()}.</li>
 *       <li>If {@link io.github.darkona.logged.Logged#logStackTrace()} is true, the {@link Throwable} is passed to SLF4J so the
 *           stack trace is rendered by the backend; otherwise only the formatted message is logged.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Formatting and interpolation</strong><br>
 * Message templates come either from {@link io.github.darkona.logged.LoggedProperties} defaults or from per-invocation overrides
 * in {@link io.github.darkona.logged.Logged}. All templates are resolved through
 * {@link io.github.darkona.logged.utils.StringInterpolator#interpolate(String, java.util.Map)} using the token map from
 * {@link io.github.darkona.logged.api.Data#tok()}. This plugin contributes the tokens:
 * <ul>
 *   <li>{@link io.github.darkona.logged.api.LogToken#DEPTH} – a visual indentation string based on nesting depth.</li>
 *   <li>{@link io.github.darkona.logged.api.LogToken#ARGUMENTS} – a printable argument list, honoring
 *       {@link io.github.darkona.logged.Logged#argValues()} and
 *       {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getArgsTemplate()} via {@link io.github.darkona.logged.api.Arg#toString(String, io.github.darkona.logged.Logged.Values)}.</li>
 * </ul>
 * Other well-known tokens (e.g. class/method/return/time) are expected to be present in {@code Data} and are not produced here.
 *
 * <p><strong>Log levels</strong><br>
 * Emission respects SLF4J level enablement. The chosen {@link org.slf4j.event.Level} comes from
 * {@link io.github.darkona.logged.Logged#level()} for call/return and {@link io.github.darkona.logged.Logged#exceptionLevel()}
 * for exceptions. No message is formatted nor logged when the target level is disabled.
 *
 * <p><strong>Non-goals</strong><br>
 * This plugin does not manage MDC or tracing. Use a dedicated MDC plugin and/or the OpenTelemetry plugin for those concerns.
 *
 * <p><strong>Thread-safety</strong><br>
 * The class is stateless aside from injected configuration and is safe to reuse across threads.
 *
 * @see io.github.darkona.logged.LoggedProperties
 * @see io.github.darkona.logged.api.Data
 * @see io.github.darkona.logged.api.LogToken
 * @see org.slf4j.Logger
 */
public class LoggedSlf4jPlugin implements LoggedPlugin {

    private final LoggedSlf4jProperties props;
    private final LogDecorator deco;

    public LoggedSlf4jPlugin(LogDecorator deco, LoggedSlf4jProperties props) {
        this.props = props;
        this.deco = deco;
    }

    @Override
    public String announceLoad() {
        return deco.custom(Green.DARK_SEA_GREEN, "@Logged-Slf4j Plugin initialized.");
    }

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {

        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());

        if (isEnabled(log, options.level())) {
            if (props.isLogDepth()) {
                var depthS = data.depth() > 0 ? Transformer.fill(data.get(LogToken.DEPTH_ICON), data.depth()) : "";
                data.addToken(LogToken.DEPTH, depthS);
            }

            if (props.isIconColors()) setColorsToIcons(data);

            logCall(log, options.level(), data, options);
        }
    }

    private void setColorsToIcons(Data data) {
        data.addToken(LogToken.ENTRY_ICON, deco.custom(props.getEntryIconColor(), data.get(LogToken.ENTRY_ICON)));
        data.addToken(LogToken.EXIT_ICON, deco.custom(props.getExitIconColor(), data.get(LogToken.EXIT_ICON)));
        data.addToken(LogToken.THROW_ICON, deco.custom(props.getThrowIconColor(), data.get(LogToken.THROW_ICON)));
        data.addToken(LogToken.DEPTH, deco.custom(props.getDepthIconColor(), data.get(LogToken.DEPTH)));
    }

    void logCall(Logger log, Level level, Data data, Logged options) {

        if (options.callMsg() != null && !options.callMsg().isEmpty()) {

            sendToLog(log, level, options.callMsg(), data.tok());

        } else if (options.onCall()) {

            if (options.args()) {
                data.addToken(LogToken.ARGUMENTS, makePrintableArgs(data.args(), options.argValues()));

                sendToLog(log, level, props.getCallMsgArgs(), data.tok());
            } else {
                sendToLog(log, level, props.getCallMsgNoArgs(), data.tok());
            }

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
        if (options.onReturn() && options.returnMsg().isEmpty()) {
            var rv = data.tokens().get(LogToken.RETURN_VALUE);
            String template = switch (options.returnValue()) {
                case ALL -> props.getExitMsgValue();
                case NULL -> rv.equals(NULL) ? props.getExitMsgValue() : props.getExitMsg();
                case NONE -> "";
            };
            if (options.time()) template += " " + props.getTimeTakenMsg();
            sendToLog(log, level, template, data.tok());
        } else if (!options.returnMsg().isEmpty()) {
            sendToLog(log, level, options.returnMsg(), data.tok());
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

        if (!options.onException() && options.exceptionMsg().isBlank()) return;

        var level = options.exceptionLevel();
        String template = options.exceptionMsg().isBlank()
                          ? props.getThrowMsg() + (options.time() ? " " + props.getTimeTakenMsg() : "")
                          : options.exceptionMsg();

        if (options.logStackTrace()) {
            String message = StringInterpolator.interpolateWithDefaults(template, data.tok());
            log.atLevel(level).log(message, e);
        } else {
            sendToLog(log, level, template, data.tok());
        }
    }

    private void sendToLog(Logger log, Level level, String template, Map<String, String> tokens) {
        log.atLevel(level).log(StringInterpolator.interpolateWithDefaults(template, tokens));
    }

    private String makePrintableArgs(Arg[] args, Logged.Values argValues) {
        return args.length > 0 ? Arrays.stream(args)
                                       .map(a -> a != null ? a.toString(props.getArgsTemplate(), argValues) : "")
                                       .collect(Collectors.joining(",")) : "";
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
