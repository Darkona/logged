package io.github.darkona.logged.weaving;

import io.github.darkona.logged.Logged;
import org.springframework.stereotype.Service;

/**
 * The method shapes a Spring AOP proxy cannot intercept, plus a public one as control.
 */
@Service
public class WeavingProbeService {

    @Logged
    public String publicMethod() {
        return "public";
    }

    @Logged
    private String privateMethod() {
        return "private";
    }

    @Logged
    public static String staticMethod() {
        return "static";
    }

    @Logged
    public final String finalMethod() {
        return "final";
    }

    @Logged
    public String selfInvoker() {
        return selfInvoked();
    }

    /** Reached only through this.selfInvoked(), a call that never crosses the proxy. */
    @Logged
    public String selfInvoked() {
        return "selfInvoked";
    }

    /** Public door to the private method, which a test cannot call directly. */
    public String callPrivate() {
        return privateMethod();
    }
}
