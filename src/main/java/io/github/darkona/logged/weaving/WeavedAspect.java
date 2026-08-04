package io.github.darkona.logged.weaving;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class WeavedAspect {

    @Around(value = "execution(* *(..)) && !execution(* lambda$*(..)) && (@annotation(io.github.darkona.logged.Logged) || @within(io.github.darkona.logged.Logged))")
    public Object logMethod(ProceedingJoinPoint pjp)
    throws Throwable {
        return LoggedBridge.log(pjp);
    }
}
