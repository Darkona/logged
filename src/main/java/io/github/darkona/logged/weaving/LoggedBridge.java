package io.github.darkona.logged.weaving;

import io.github.darkona.logged.internals.LoggedEngine;
import org.aspectj.lang.ProceedingJoinPoint;

public class LoggedBridge {
    private static volatile LoggedEngine delegate;

    public static void install(LoggedEngine d) {
        delegate = d;
    }

    /**
     * Clears the delegate only if it still belongs to the closing context, so a
     * closed ApplicationContext is not kept reachable through this static field.
     */
    public static void uninstall(LoggedEngine d) {
        if (delegate == d) delegate = null;
    }

    public static Object log(ProceedingJoinPoint pjp)
    throws Throwable {
        if (delegate != null) return delegate.logMethod(pjp);
        return pjp.proceed();
    }
}
