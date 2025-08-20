package io.github.darkona.logged.internals;

import io.github.darkona.logged.LoggedAspect;
import org.aspectj.lang.ProceedingJoinPoint;

public class LoggedBridge {
    private static volatile LoggedAspect delegate;
    public static void install(LoggedAspect d) {
        delegate = d;
    }
    static void log(ProceedingJoinPoint pjp) throws Throwable {
        if (delegate != null) {
            delegate.logMethod(pjp);
        }
    }
}
