package io.github.darkona.logged.plugins.mdc;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.utils.Transformer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
 * MDC plugin for {@code @Logged} that writes per-invocation metadata into SLF4J’s
 * {@link org.slf4j.MDC} and removes those entries when the invocation completes.
 * <p>
 * <strong>Behavior</strong>
 * <ul>
 *   <li><b>onCall</b> (when {@link io.github.darkona.logged.Logged#onCall()} is {@code true}):
 *     <ul>
 *       <li>Puts {@code class} with {@link io.github.darkona.logged.api.LogToken#CLASS_NAME}.</li>
 *       <li>Puts {@code method} with {@link io.github.darkona.logged.api.LogToken#METHOD_NAME}.</li>
 *       <li>If {@link io.github.darkona.logged.Logged#args()} is {@code true}, puts
 *           {@code args} with {@link io.github.darkona.logged.api.LogToken#ARGUMENTS} (the printable,
 *           already-sanitized representation provided by {@code Data}).</li>
 *     </ul>
 *   </li>
 *   <li><b>onReturn</b>:
 *     <ul>
 *       <li>Puts {@code outcome=ok}.</li>
 *       <li>If {@link io.github.darkona.logged.Logged#onReturn()} is {@code true}, puts
 *           {@code result} with {@link io.github.darkona.logged.api.LogToken#RETURN_VALUE}, truncated to 2048 chars.</li>
 *       <li>If {@link io.github.darkona.logged.Logged#time()} is {@code true}, puts
 *           {@code latency_ms} with {@link io.github.darkona.logged.api.LogToken#DURATION}.</li>
 *       <li>Finally removes all keys managed by this plugin from the MDC.</li>
 *     </ul>
 *   </li>
 *   <li><b>onException</b>:
 *     <ul>
 *       <li>Puts {@code outcome=error}.</li>
 *       <li>Puts {@code exception} with {@link io.github.darkona.logged.api.LogToken#EXCEPTION_CLASS}.</li>
 *       <li>If {@link io.github.darkona.logged.Logged#onException()} is {@code true}, puts
 *           {@code exception_msg} with {@link Throwable#getMessage()} (not truncated here).</li>
 *       <li>Finally removes all keys managed by this plugin from the MDC.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * <strong>MDC keys written by this plugin</strong>
 * <ul>
 *   <li>{@code class} – simple or configured class name of the invocation.</li>
 *   <li>{@code method} – method name.</li>
 *   <li>{@code args} – printable argument list (if enabled).</li>
 *   <li>{@code result} – printable return value (if enabled; truncated to 2048 chars).</li>
 *   <li>{@code outcome} – {@code ok} or {@code error}.</li>
 *   <li>{@code exception} – exception simple class name on failures.</li>
 *   <li>{@code exception_msg} – exception message on failures (if enabled).</li>
 *   <li>{@code latency_ms} – duration (only on successful return when {@code time==true}).</li>
 * </ul>
 *
 * <p>
 * <strong>Scope and cleanup</strong><br>
 * MDC is thread-local; server thread pools reuse threads across requests. To avoid context leakage,
 * this plugin removes <em>only</em> the keys it owns after each return/exception, leaving unrelated
 * MDC entries (e.g., a correlation ID set by an HTTP filter) untouched. The implementation does not
 * perform push/pop restoration for nested {@code @Logged} calls; inner calls temporarily overwrite
 * these keys and clear them on exit. If you need full nesting restoration, extend this plugin with a
 * per-thread stack of previous values.
 *
 * <p>
 * <strong>Separation of concerns</strong><br>
 * This plugin does not write tracing identifiers. Use the OpenTelemetry plugin to populate
 * {@code trace_id}/{@code span_id} (or to correlate logs with traces).
 * The printable argument/return representations are expected to be pre-sanitized upstream.
 *
 * <p>
 * <strong>Output formatting</strong><br>
 * Use {@code %X{key}} placeholders in your logging pattern (or JSON encoders) to include these MDC
 * fields in log lines.
 *
 * <p>
 * <strong>Initialization banner</strong><br>
 * {@link io.github.darkona.logged.api.LogDecorator} is only used for the colored initialization message.
 *
 * @see org.slf4j.MDC
 * @see io.github.darkona.logged.api.LogToken
 * @see io.github.darkona.logged.plugins.otel.LoggedOpenTelemetryPlugin
 */

/**
 * MDC plugin for @Logged.
 * Writes per-invocation metadata to SLF4J MDC and clears it on completion.
 */
public class LoggedMdcPlugin implements LoggedPlugin {


    private static final String CLASS = "class";
    private static final String METHOD = "method";
    private static final String METHOD_TYPE = "method_type";
    private static final String ARGS = "args";
    private static final String RESULT = "result";
    private static final String RESULT_TYPE = "result_type";
    private static final String OUTCOME = "outcome";
    private static final String EXCEPTION = "exception";
    private static final String EXCEPTION_MSG = "exception_msg";
    private static final String LATENCY_MS = "latency_ms";

    private static final List<String> ALL = Arrays.asList(CLASS, METHOD, ARGS, RESULT, OUTCOME, EXCEPTION, EXCEPTION_MSG, LATENCY_MS);
    private static final Logger log = LoggerFactory.getLogger(LoggedMdcPlugin.class);

    private final LogDecorator deco;
    private final LoggedMdcProperties props;

    public LoggedMdcPlugin(LogDecorator deco, LoggedMdcProperties props) {
        this.deco = deco;
        this.props = props;
    }

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
        if (!props.isEnabled()) return;
        log.debug(deco.orange("MDC Plugin called"));
        loadCallData(data, options);
    }

    private void loadCallData(Data data, Logged options) {
        if (options.onCall()) {
            MDC.put(CLASS, data.get(LogToken.CLASS_LONG));
            MDC.put(METHOD, data.get(LogToken.METHOD_NAME));
            MDC.put(METHOD_TYPE, data.get(LogToken.METHOD_TYPE));
            if (options.args()) {
                data.addToken(LogToken.ARGUMENTS, makePrintableArgs(data.args(), options.argValues()));
                MDC.put(ARGS, data.get(LogToken.ARGUMENTS));
            }
            if (props.isAddMark()) {
                MDC.put(props.getMarkerKey(), props.getMarkerValue());
            }
        }
    }

    private String makePrintableArgs(Arg[] args, Logged.Values argValues) {
        return args.length > 0 ? "[" + Arrays.stream(args)
                                             .map(a -> a != null ? a.toString(props.getArgsTemplate(), argValues, props.getMaxValueLength()) : "")
                                             .collect(Collectors.joining(", ")) + "]" : "[]";
    }

    @Override
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
        if (!props.isEnabled()) return;
        loadCallData(data, options);
        MDC.put(OUTCOME, "ok");
        if (options.onReturn()) {
            MDC.put(RESULT, Transformer.truncate(data.get(LogToken.RETURN_VALUE), props.getMaxValueLength()));
            MDC.put(RESULT_TYPE, data.get(LogToken.RETURN_CLASS));
        }
        if (options.time()) {
            MDC.put(LATENCY_MS, data.get(LogToken.DURATION));
        }
    }

    @Override
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception) {
        if (!props.isEnabled()) return;
        loadCallData(data, options);
        MDC.put(OUTCOME, "error");
        MDC.put(EXCEPTION, data.get(LogToken.EXCEPTION_CLASS));
        if (options.onException()) {
            MDC.put(EXCEPTION_MSG, exception.getMessage());
        }
    }

    @Override
    public String announceLoad() {
        return deco.blue("@Logged-MDC Plugin initialized.");
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void afterMethod() {
        clearMDC();
    }


    private void clearMDC() {
        for (var s : ALL) {
            MDC.remove(s);
        }
    }
}
