package io.github.darkona.logged.api;

import io.github.darkona.logged.Logged;
import org.aspectj.lang.ProceedingJoinPoint;

public interface LoggedPlugin {

    void onCall(ProceedingJoinPoint pjp, Data data, Logged options);

    void onReturn(ProceedingJoinPoint pjp, Data data, Logged options);

    void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception);

    String announceLoad();
}
