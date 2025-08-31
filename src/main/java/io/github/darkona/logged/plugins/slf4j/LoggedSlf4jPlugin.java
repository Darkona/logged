package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.BasicColor;
import io.github.darkona.logged.colors.ColorEnum;
import io.github.darkona.logged.colors.ColorFinder;
import io.github.darkona.logged.colors.Green;
import io.github.darkona.logged.colors.Orange;
import io.github.darkona.logged.utils.StringInterpolator;
import io.github.darkona.logged.utils.Transformer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

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
 *           token built from {@link io.github.darkona.logged.api.Arg#toString(String, io.github.darkona.logged.Logged.Values, int)}.</li>
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
 *       {@link io.github.darkona.logged.plugins.slf4j.LoggedSlf4jProperties#getArgsTemplate()} via {@link io.github.darkona.logged.api.Arg#toString(String, io.github.darkona.logged.Logged.Values, int)}.</li>
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

    private static final Logger log = LoggerFactory.getLogger(LoggedSlf4jPlugin.class);
    private final LoggedSlf4jProperties props;
    private final LogDecorator deco;

    private ColorEnum entryIconColor = BasicColor.BLUE;
    private ColorEnum exitIconColor = BasicColor.GREEN;
    private ColorEnum throwIconColor = BasicColor.RED;
    private ColorEnum depthIconColor = Orange.ORANGE;
    private LoggedProperties rootProps;

    public LoggedSlf4jPlugin(LogDecorator deco, LoggedSlf4jProperties props) {
        this.props = props;
        this.deco = deco;

        if (props.isIconColors()) {
            entryIconColor = ColorFinder.findColor(props.getEntryIconColor());
            exitIconColor = ColorFinder.findColor(props.getExitIconColor());
            throwIconColor = ColorFinder.findColor(props.getThrowIconColor());
            depthIconColor = ColorFinder.findColor(props.getDepthIconColor());
        }
    }

    public void setRootProps(LoggedProperties rootProps) {
        this.rootProps = rootProps;
    }

    @Override
    public String announceLoad() {
        var s = deco.custom(Green.DARK_SEA_GREEN, "@Logged-Slf4j Plugin initialized.");
        if (props.isKeyValue())
            s += "\n" + deco.custom(Green.DARK_SEA_GREEN, "@Logged-Slf4j Key-Value capability enabled.");
        return s;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void afterMethod() {

    }

    /** Handles method entry logging if the level is enabled. */
    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
        if (!props.isEnabled()) return;
        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        if (isEnabled(log, options.level())) {
            captureMdc(data);
            if (props.isLogDepth()) {
                var depthS = data.depth() > 0 ? Transformer.fill(data.get(LogToken.DEPTH_ICON), data.depth()) : "";
                data.addToken(LogToken.DEPTH, depthS);
            }

            if (props.isIconColors()) setColorsToIcons(data);

            logCall(log, options.level(), data, options);
        }
    }

    private void captureMdc(Data data) {
        if (!props.getCaptureFromMdc().isEmpty()) {
            for (var s : props.getCaptureFromMdc()) {
                if (s != null) data.addFlexToken(LogToken.MDC.token() + s, MDC.get(s) != null ? MDC.get(s) : NULL);
            }
        }
    }

    private void setColorsToIcons(Data data) {

        data.addToken(LogToken.ENTRY_ICON, deco.custom(entryIconColor, data.get(LogToken.ENTRY_ICON)));
        data.addToken(LogToken.EXIT_ICON, deco.custom(exitIconColor, data.get(LogToken.EXIT_ICON)));
        data.addToken(LogToken.THROW_ICON, deco.custom(throwIconColor, data.get(LogToken.THROW_ICON)));
        data.addToken(LogToken.DEPTH, deco.custom(depthIconColor, data.get(LogToken.DEPTH)));
    }

    private void logCall(Logger log, Level level, Data data, Logged options) {
        if (!options.callMsg().isEmpty()) {

            sendToLog(log, level, options.callMsg(), data.tok(), options, null);

        } else if (options.onCall()) {

            if (options.args()) {
                data.addToken(LogToken.ARGUMENTS, makePrintableArgs(data.args(), options.argValues()));
                sendToLog(log, level, props.getCallMsgArgs(), data.tok(), options, null);
            } else {
                sendToLog(log, level, props.getCallMsgNoArgs(), data.tok(), options, null);
            }

        }
    }

    private static final String SLOW_MARKER_KEY = "__logged.slow.marker";

    private Level applyThresholdIfNeeded(Data data, Logged options, Level baseLevel) {
        try {
            long perMethod = options.warnIfOverMs();
            long global = (rootProps != null && rootProps.getThreshold() != null) ? rootProps.getThreshold().getWarnMs() : -1L;
            long threshold = perMethod >= 0 ? perMethod : global;
            if (threshold < 0) return baseLevel;
            String d = data.get(LogToken.DURATION);
            long duration = d != null && !d.isBlank() ? Long.parseLong(d) : -1L;
            if (duration > threshold) {
                if (options.slowMarker() != null && !options.slowMarker().isBlank()) {
                    data.addFlexToken(SLOW_MARKER_KEY, options.slowMarker());
                }
                org.slf4j.event.Level promote = (rootProps != null && rootProps.getThreshold() != null)
                        ? rootProps.getThreshold().getPromoteLevel() : Level.WARN;
                return higherOf(baseLevel, promote);
            }
        } catch (Exception ignored) { }
        return baseLevel;
    }

    private Level higherOf(Level a, Level b) {
        int ia = severity(a);
        int ib = severity(b);
        return (ib > ia) ? b : a;
    }

    private int severity(Level l) {
        return switch (l) {
            case TRACE -> 0;
            case DEBUG -> 1;
            case INFO -> 2;
            case WARN -> 3;
            case ERROR -> 4;
        };
    }

    @Override
    /** Handles method return logging if the level is enabled. */
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
        if (!props.isEnabled()) return;
        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        Level eff = applyThresholdIfNeeded(data, options, options.level());
        if (isEnabled(log, eff)) {
            captureMdc(data);
            logReturn(log, eff, data, options);
        }
    }

    private void logReturn(Logger log, Level level, Data data, Logged options) {
        if (options.onReturn() && options.returnMsg().isEmpty()) {
            var rv = data.tokens().get(LogToken.RETURN_VALUE);
            String template = switch (options.returnValue()) {
                case ALL -> props.getExitMsgValue();
                case NULL -> rv.equals(NULL) ? props.getExitMsgValue() : props.getExitMsg();
                case NONE -> "";
            };
            if (template.isEmpty()) return;
            if (options.time()) template += " " + props.getTimeTakenMsg();
            sendToLog(log, level, template, data.tok(), options, null);
        } else if (!options.returnMsg().isEmpty()) {
            sendToLog(log, level, options.returnMsg(), data.tok(), options, null);
        }
    }

    @Override
    /** Handles exception logging if the level is enabled. */
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception) {
        if (!props.isEnabled()) return;
        Logger log = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        Level eff = applyThresholdIfNeeded(data, options, options.exceptionLevel());
        if (isEnabled(log, eff)) {
            captureMdc(data);
            logException(log, exception, data, options, eff);
        }
    }

    private void logException(Logger log, Throwable e, Data data, Logged options, Level level) {

        if (!options.onException() && options.exceptionMsg().isBlank()) return;

        String template = options.exceptionMsg().isBlank()
                          ? props.getThrowMsg() + (options.time() ? " " + props.getTimeTakenMsg() : "")
                          : options.exceptionMsg();

        if (options.logStackTrace()) {
            sendToLog(log, level, template, data.tok(), options, e);
        } else {
            sendToLog(log, level, template, data.tok(), options, null);
        }
    }

    private void sendToLog(Logger log, Level level, String template, Map<String, String> tokens, Logged options, Throwable ex) {
        if (template == null || template.isEmpty()) return;
        LoggingEventBuilder builder = log.atLevel(level);

        String rvKey = LogToken.RETURN_VALUE.token();
        String rv = tokens.get(rvKey);
        if (rv != null) {
            tokens.put(rvKey, Transformer.truncate(rv, props.getMaxValueLength()));
        }

        builder = setKeyValuePairs(builder, tokens);

        int optLen = options.markers().length;
        int propLen = props.getMarkers().length;
        if (optLen + propLen > 0) {
            String[] markers = Arrays.copyOf(options.markers(), optLen + propLen);
            if (propLen > 0) System.arraycopy(props.getMarkers(), 0, markers, optLen, propLen);
            for (String marker : markers) {
                if (marker != null && !marker.isBlank()) {
                    builder = builder.addMarker(MarkerFactory.getMarker(marker));
                }
            }
        }

        String slowMarker = tokens.remove(SLOW_MARKER_KEY);
        if (slowMarker != null && !slowMarker.isBlank()) {
            builder = builder.addMarker(MarkerFactory.getMarker(slowMarker));
        }

        if (ex != null) builder = builder.setCause(ex);

        builder.log(StringInterpolator.interpolateWithDefaults(template, tokens));
    }


    private LoggingEventBuilder setKeyValuePairs(LoggingEventBuilder builder, Map<String, String> data) {

        if (!props.isKeyValue()) return builder;

        for (LogToken tok : props.getKeyValues()) {
            if (data.get(tok.name()) != null) {
                builder = builder.addKeyValue(tok.name(), data.get(tok.token()));
            }
        }
        return builder;
    }

    private String makePrintableArgs(Arg[] args, Logged.Values argValues) {
        if (args.length == 0) return "";
        StringBuilder sb = new StringBuilder(args.length * 16);
        for (int i = 0; i < args.length; i++) {
            Arg a = args[i];
            if (a != null) sb.append(a.toString(props.getArgsTemplate(), argValues, props.getMaxValueLength()));
            if (i < args.length - 1) sb.append(", ");
        }
        return sb.toString();
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
