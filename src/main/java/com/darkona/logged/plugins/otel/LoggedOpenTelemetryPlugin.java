package com.darkona.logged.plugins.otel;


import com.darkona.logged.Data;
import com.darkona.logged.Logged;
import com.darkona.logged.LoggedAspect;
import com.darkona.logged.colors.Blue;
import com.darkona.logged.internals.LogDecorator;
import com.darkona.logged.internals.LogToken;
import com.darkona.logged.plugins.LoggedPlugin;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class LoggedOpenTelemetryPlugin implements LoggedPlugin {

    private final LogDecorator deco;
    private final Tracer tracer;

    private final ThreadLocal<Deque<Span>> spanStack = ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Scope>> scopeStack = ThreadLocal.withInitial(ArrayDeque::new);

    public LoggedOpenTelemetryPlugin(LogDecorator deco, Tracer tracer) {
        this.deco = deco;
        this.tracer = tracer;
    }

    @PostConstruct
    void init() {
        LoggerFactory.getLogger(LoggedAspect.class)
                     .info(deco.custom(Blue.CORNFLOWER_BLUE, "@Logged-OpenTelemetry Plugin initialized."));
    }

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
        String spanName = data.tokens().get(LogToken.CLASS_NAME) + "::" + data.tokens().get(LogToken.METHOD_NAME);

        Span span = tracer.spanBuilder(spanName)
                          .setParent(Context.current())
                          .setSpanKind(SpanKind.INTERNAL)
                          .startSpan();

        Scope scope = span.makeCurrent();
        spanStack.get().push(span);
        scopeStack.get().push(scope);
    }

    @Override
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
        Span span = safePop(spanStack);
        Scope scope = safePop(scopeStack);

        if (span != null && span.getSpanContext().isValid()) {
            span.end();
        }
        if (scope != null) scope.close();
    }

    @Override
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable ex) {
        Span span = safePop(spanStack);
        Scope scope = safePop(scopeStack);
        if (span != null && span.getSpanContext().isValid()) {
            if (options.onException() && ex != null) {
                span.recordException(ex);
                span.setStatus(StatusCode.ERROR, ex.getMessage());
            }
            span.end();
        }
        if (scope != null) scope.close();
    }

    private <T> T safePop(ThreadLocal<Deque<T>> stackThreadLocal) {
        Deque<T> stack = stackThreadLocal.get();
        return stack.isEmpty() ? null : stack.pop();
    }
}

