package io.github.darkona.logged.weaving;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LoggedPlugin;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts interception events per method name. Counting through the plugin API rather than
 * parsing log output: double instrumentation shows up as a count of 2 whatever the layout
 * does to the text.
 */
@Component
public class CountingPlugin implements LoggedPlugin {

    private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> returns = new ConcurrentHashMap<>();

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
        calls.computeIfAbsent(pjp.getSignature().getName(), k -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) {
        returns.computeIfAbsent(pjp.getSignature().getName(), k -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception) {
    }

    @Override
    public String announceLoad() {
        return "weaving count probe";
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void afterMethod() {
    }

    public int calls(String method) {
        var counter = calls.get(method);
        return counter == null ? 0 : counter.get();
    }

    public int returns(String method) {
        var counter = returns.get(method);
        return counter == null ? 0 : counter.get();
    }

    public void reset() {
        calls.clear();
        returns.clear();
    }
}
