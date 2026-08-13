package io.github.darkona.logged.internals;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LoggedPlugin;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginDispatcherTest {

    private static class CountingPlugin implements LoggedPlugin {
        final AtomicInteger loads = new AtomicInteger();
        final AtomicInteger calls = new AtomicInteger();
        final boolean explode;

        CountingPlugin(boolean explode) { this.explode = explode; }

        @Override
        public void onLoad() {
            loads.incrementAndGet();
            if (explode) throw new IllegalStateException("boom on load");
        }

        @Override
        public String announceLoad() { return ""; }

        @Override
        public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) {
            calls.incrementAndGet();
            if (explode) throw new IllegalStateException("boom on call");
        }

        @Override
        public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) { }

        @Override
        public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable e) { }

        @Override
        public void afterMethod() { }
    }

    @Test
    void throwingPluginDoesNotStopDispatch() {
        var bad = new CountingPlugin(true);
        var good = new CountingPlugin(false);
        var dispatcher = new PluginDispatcher(List.of(bad, good));

        assertDoesNotThrow(() -> dispatcher.dispatch("onCall", p -> p.onCall(null, null, null)));
        assertEquals(1, bad.calls.get());
        assertEquals(1, good.calls.get());
    }

    @Test
    void throwingPluginDoesNotStopLoadAll() {
        var bad = new CountingPlugin(true);
        var good = new CountingPlugin(false);
        var dispatcher = new PluginDispatcher(List.of(bad, good));

        assertDoesNotThrow(() -> dispatcher.loadAll(true));
        assertEquals(1, bad.loads.get());
        assertEquals(1, good.loads.get());
    }
}
