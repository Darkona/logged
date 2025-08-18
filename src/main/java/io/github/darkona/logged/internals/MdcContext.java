package io.github.darkona.logged.internals;

import io.github.darkona.logged.LoggedProperties;
import org.slf4j.MDC;

import java.util.Map;


public class MdcContext {

    private LoggedProperties props;

    private MdcContext() {}

    public static String asPrefix() {

        Map<String, String> ctx = MDC.getCopyOfContextMap();
        if (ctx == null || ctx.isEmpty()) return "";
        String trace = ctx.getOrDefault("traceId", ctx.getOrDefault("trace_id", ""));
        String span  = ctx.getOrDefault("spanId", ctx.getOrDefault("span_id", ""));
        if (trace.isEmpty() && span.isEmpty()) return "";
        if (span.isEmpty()) return "[traceId=" + trace + "] ";
        if (trace.isEmpty()) return "[spanId=" + span + "] ";
        return "[traceId=" + trace + " spanId=" + span + "] ";
    }

}
