package io.github.darkona.logged.internals;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggedAspect {

    private final LoggedEngine loggedEngine;

    public LoggedAspect(LoggedEngine loggedEngine) {
        this.loggedEngine = loggedEngine;
    }

    @Around(value = "execution(* *(..)) && !execution(* lambda$*(..)) && (@annotation(io.github.darkona.logged.Logged) || @within(io.github.darkona.logged.Logged))")
    public Object logMethod(ProceedingJoinPoint pjp)
    throws Throwable {
        return loggedEngine.logMethod(pjp);
    }
}
