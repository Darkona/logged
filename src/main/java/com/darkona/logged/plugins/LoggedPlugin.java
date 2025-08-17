package com.darkona.logged.plugins;

import com.darkona.logged.Data;
import com.darkona.logged.Logged;
import com.darkona.logged.internals.LogToken;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Map;

public interface LoggedPlugin {

    void onCall(ProceedingJoinPoint  pjp, Data data, Logged options);
    void onReturn(ProceedingJoinPoint  pjp, Data data, Logged options);
    void onException(ProceedingJoinPoint  pjp, Data data, Logged options, Throwable exception);
}
