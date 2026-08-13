package io.github.darkona.logged.plugins.otel;


import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Arg;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.Red;
import io.github.darkona.logged.utils.StringInterpolator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * OpenTelemetry plugin for @Logged.
 * Creates INTERNAL spans and adds basic code/args metadata; respects enablement.
 */
/*
 * OpenTelemetry plugin for {@code @Logged} that creates a new {@link io.opentelemetry.api.trace.Span}
 * for each intercepted method invocation and enriches it with code, call-depth and argument metadata.
 *
 * Behavior
 * <ul>
 *   <li><b>onCall</b>: builds and starts an INTERNAL span named
 *       {@code <code.namespace>#<code.function>} using the current context as parent.
 *       If available, file name and line number are recorded from {@link org.aspectj.lang.ProceedingJoinPoint#getSourceLocation()}.</li>
 *   <li><b>onReturn</b>: sets {@link io.opentelemetry.api.trace.StatusCode#OK} and, when enabled by {@link io.github.darkona.logged.Logged#onReturn()},
 *       records the declared return type and a {@code logged.return.null} flag if the return type was reported as {@code "null"}.</li>
 *   <li><b>onException</b>: when enabled by {@link io.github.darkona.logged.Logged#onException()}, records the exception on the span and
 *       sets {@link io.opentelemetry.api.trace.StatusCode#ERROR} with a short description.</li>
 * </ul>
 *
 * <p>
 * <strong>Span attributes</strong> (keys follow OpenTelemetry code semantic attributes plus {@code logged.*} custom attributes):
 * <ul>
 *   <li>{@code code.namespace} – fully qualified class name of the intercepted type.</li>
 *   <li>{@code code.function} – method name.</li>
 *   <li>{@code code.filepath} – source file name (when available).</li>
 *   <li>{@code code.lineno} – 1-based source line number (when available).</li>
 *   <li>{@code logged.depth} – nesting depth of {@code @Logged} interceptions for the current call chain.</li>
 *   <li>{@code logged.args.count} – number of arguments observed by the aspect.</li>
 *   <li>{@code logged.args.names} – ordered list of argument names.</li>
 *   <li>{@code logged.args.masked} – names of arguments whose <em>values</em> are configured to be masked
 *       (derived from {@link io.github.darkona.logged.Logged#maskArgValues()} and {@link io.github.darkona.logged.Logged#maskAtPos()}).</li>
 *   <li>{@code logged.return.type} – declared simple name of the return type (reported on return when enabled).</li>
 *   <li>{@code logged.return.null} – {@code true} if the declared return type string equals {@code "null"}.</li>
 * </ul>
 *
 * <p>
 * <strong>Nesting and thread safety</strong><br>
 * The plugin maintains a per-thread {@link java.util.Deque} of spans via {@link ThreadLocal} to correctly handle
 * nested {@code @Logged} interceptions. Each {@code onCall} pushes the started span; {@code onReturn}/{@code onException}
 * always end and pop the top span, removing the {@code ThreadLocal} when the stack becomes empty. This design
 * is safe for typical server-side thread pools.
 *
 * <p>
 * <strong>Inputs</strong><br>
 * The plugin expects the following from {@link io.github.darkona.logged.api.Data} and {@link io.github.darkona.logged.api.LogToken}:
 * <ul>
 *   <li>{@link io.github.darkona.logged.api.LogToken#CLASS_LONG} – fully qualified class name.</li>
 *   <li>{@link io.github.darkona.logged.api.LogToken#METHOD_NAME} – method name.</li>
 *   <li>{@link io.github.darkona.logged.api.LogToken#RETURN_CLASS} – return type simple name (for {@code onReturn}).</li>
 *   <li>{@link io.github.darkona.logged.api.Data#depth()} – current interception depth.</li>
 *   <li>{@link io.github.darkona.logged.api.Data#args()} and {@link io.github.darkona.logged.api.Data#argNames()} – argument details.</li>
 * </ul>
 *
 * <p>
 * <strong>Configuration knobs</strong>
 * <ul>
 *   <li>{@link io.github.darkona.logged.Logged#onReturn()} – controls whether return metadata is added.</li>
 *   <li>{@link io.github.darkona.logged.Logged#onException()} – controls whether exceptions are recorded to the span.</li>
 *   <li>{@link io.github.darkona.logged.Logged#maskArgValues()} / {@link io.github.darkona.logged.Logged#maskAtPos()} –
 *       define which argument <em>values</em> should be treated as masked; the plugin emits their names in
 *       {@code logged.args.masked} but does not serialize argument values into span attributes.</li>
 * </ul>
 *
 * <p>
 * <strong>Notes</strong>
 * <ul>
 *   <li>Spans are created with {@link io.opentelemetry.api.trace.SpanKind#INTERNAL} and
 *       {@link io.opentelemetry.context.Context#current()} as parent, so they compose with existing traces
 *       from HTTP/RPC instrumentation or the OpenTelemetry Java agent.</li>
 *   <li>Source location attributes ({@code code.filepath}/{@code code.lineno}) depend on runtime availability of
 *       source information from the join point and may be absent in some deployments.</li>
 *   <li>No MDC manipulation is performed here; MDC concerns should live in a separate plugin.</li>
 * </ul>
 *
 * <p>
 * <strong>Dependencies</strong><br>
 * Requires a {@link io.opentelemetry.api.trace.Tracer} and a {@link io.github.darkona.logged.api.LogDecorator}
 * (the latter only for initialization banner formatting).
 */

public class LoggedOpenTelemetryPlugin implements LoggedPlugin {

    private static final AttributeKey<String> CODE_NAMESPACE = AttributeKey.stringKey("code.namespace");
    private static final AttributeKey<String> CODE_FUNCTION = AttributeKey.stringKey("code.function");
    private static final AttributeKey<String> CODE_FILEPATH = AttributeKey.stringKey("code.filepath");
    private static final AttributeKey<Long> CODE_LINENO = AttributeKey.longKey("code.lineno");

    private static final AttributeKey<Long> LOGGED_DEPTH = AttributeKey.longKey("logged.depth");
    private static final AttributeKey<Long> LOGGED_ARGS_COUNT = AttributeKey.longKey("logged.args.count");
    private static final AttributeKey<List<String>> LOGGED_ARGS_NAMES = AttributeKey.stringArrayKey("logged.args.names");
    private static final AttributeKey<List<String>> LOGGED_ARGS_MASKED = AttributeKey.stringArrayKey("logged.args.masked");
    private static final AttributeKey<String> LOGGED_RETURN_TYPE = AttributeKey.stringKey("logged.return.type");
    private static final AttributeKey<Boolean> LOGGED_RETURN_NULL = AttributeKey.booleanKey("logged.return.null");
    private static final Logger log = LoggerFactory.getLogger(LoggedOpenTelemetryPlugin.class);
    private final LogDecorator deco;
    private final Tracer tracer;
    private final LoggedOpenTelemetryProperties props;
    /**
     * Span plus the state to restore when it ends: its Scope (makeCurrent) and the
     * MDC value the span replaced, so nested @Logged calls restore the outer one.
     */
    private record SpanEntry(Span span, Scope scope, String previousMdcValue) {}

    private final ThreadLocal<Deque<SpanEntry>> spanStack = ThreadLocal.withInitial(ArrayDeque::new);

    private final StringInterpolator.Template spanIdTemplate;

    public LoggedOpenTelemetryPlugin(LogDecorator deco, LoggedOpenTelemetryProperties props, Tracer tracer) {
        this.deco = deco;
        this.tracer = tracer;
        this.props = props;
        this.spanIdTemplate = StringInterpolator.compile(props.getSpanIdTemplate());
    }

    @Override
    public String announceLoad() {
        return deco.custom(Red.RED, "@Logged-OpenTelemetry Plugin initialized.");
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void afterMethod() {
        // MDC restoration happens in endTopSpan, paired with the span that set it
    }

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
        if (!props.isEnabled()) return;
        if (log.isDebugEnabled()) log.debug(deco.red("Otel Plugin called"));
        String spanName = spanIdTemplate.render(data.tok());

        var builder = tracer.spanBuilder(spanName)
                            .setParent(Context.current())
                            .setSpanKind(SpanKind.INTERNAL);

        if (props.isAddClass()) {
            builder.setAttribute(CODE_NAMESPACE, data.get(LogToken.CLASS_LONG));
        }

        if (props.isAddMethod()) {
            builder.setAttribute(CODE_FUNCTION, data.get(LogToken.METHOD_NAME));
        }

        if (props.isAddSourceLine()) {
            var loc = pjp.getSourceLocation();
            if (loc != null) {
                try {
                    var file = loc.getFileName();
                    var line = loc.getLine();
                    if (file != null && !file.isBlank()) builder.setAttribute(CODE_FILEPATH, file);
                    if (line > 0) builder.setAttribute(CODE_LINENO, (long) line);
                }catch (UnsupportedOperationException e) {
                    log.debug("Can't obtain line number or filename from  {}", data.get(LogToken.CLASS_LONG));
                }
            }
        }

        if (props.isAddDepth()) {
            builder.setAttribute(LOGGED_DEPTH, data.depth());
        }

        if (props.isAddArgs()) {
            Arg[] args = data.args();
            int argCount = (args == null) ? 0 : args.length;
            builder.setAttribute(LOGGED_ARGS_COUNT, (long) argCount);
            builder.setAttribute(LOGGED_ARGS_NAMES, data.argNames());
            builder.setAttribute(LOGGED_ARGS_MASKED, maskedArgNames(options, args));
        }

        String previousMdcValue = null;
        if (props.isAddToMdc()) {
            previousMdcValue = MDC.get(props.getMdcKey());
            MDC.put(props.getMdcKey(), spanName);
        }
        Span span = builder.startSpan();
        // makeCurrent so nested @Logged spans parent to this one and Span.current() is correct
        Scope scope = span.makeCurrent();
        spanStack.get().push(new SpanEntry(span, scope, previousMdcValue));
    }


    @Override
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
        if (!props.isEnabled()) return;
        Span span = safePeekSpan();

        if (span != null && span.getSpanContext().isValid()) {
            if (options.onReturn()) {
                var returnType = data.get(LogToken.RETURN_CLASS);
                if ("null".equalsIgnoreCase(returnType)) {
                    span.setAttribute(LOGGED_RETURN_NULL, true);
                }
                if (props.isAddReturnType()) {
                    span.setAttribute(LOGGED_RETURN_TYPE, data.get(LogToken.RETURN_CLASS));
                }
            }
            span.setStatus(StatusCode.OK);
        }

        endTopSpan();
    }

    @Override
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable ex) {
        if (!props.isEnabled()) return;
        Span span = safePeekSpan();
        if (span != null && span.getSpanContext().isValid()) {
            if (options.onException() && ex != null) {
                span.recordException(ex);
                if (props.isAddExceptionMsg()) {
                    span.setStatus(StatusCode.ERROR, ex.toString());
                } else {
                    span.setStatus(StatusCode.ERROR);
                }
            }
        }
        endTopSpan();
    }

    private List<String> maskedArgNames(Logged options, Arg[] args) {
        List<String> out = new ArrayList<>();
        if (options == null || args == null) return out;

        for (String rn : options.maskArgValues()) {
            if (rn != null && !rn.isBlank()) out.add(rn);
        }

        for (int p : options.maskAtPos()) {
            if (p >= 0 && p < args.length) {
                out.add(args[p].name());
            }
        }
        return out;
    }

    private Span safePeekSpan() {
        Deque<SpanEntry> stack = spanStack.get();
        return stack.isEmpty() ? null : stack.peek().span();
    }

    private void endTopSpan() {
        Deque<SpanEntry> s = spanStack.get();
        if (s.isEmpty()) {
            spanStack.remove();
            return;
        }
        SpanEntry entry = s.pop();
        try {
            entry.scope().close();
            entry.span().end();
        } finally {
            if (props.isAddToMdc()) {
                if (entry.previousMdcValue() != null) {
                    MDC.put(props.getMdcKey(), entry.previousMdcValue());
                } else {
                    MDC.remove(props.getMdcKey());
                }
            }
            if (s.isEmpty()) spanStack.remove();
        }
    }

}
