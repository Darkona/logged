package io.github.darkona.logged.weaving;

import io.github.darkona.logged.internals.LoggedEngine;
import org.aspectj.lang.ProceedingJoinPoint;

public class LoggedBridge {
    private static volatile LoggedEngine delegate;

    public static void install(LoggedEngine d) {
        delegate = d;
    }

    public static Object log(ProceedingJoinPoint pjp)
    throws Throwable {
        if (delegate != null) return delegate.logMethod(pjp);
        return pjp.proceed();
    }
}
