package io.github.darkona.logged;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggedAspectJ {

    private static volatile LoggedAspect loggedAspect;

    public static void installLoggedAspect(final LoggedAspect bean) {
        loggedAspect = bean;
    }

    @Around(value = "@annotation(io.github.darkona.logged.Logged) ||  @within(io.github.darkona.logged.Logged)")
    public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {

        return loggedAspect.logMethod(pjp);
    }
}
