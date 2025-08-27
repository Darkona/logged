package io.github.darkona.logged;

import io.github.darkona.logged.api.LogDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("UnusedReturnValue")
@Component
public class TestObject {

    @Autowired
    LogDecorator deco;

    @Logged
    public void methodWithArgs(String str, int num) {
        System.out.println("Processing string: " + str + ", number: " + num);
    }

    @Logged(args = false)
    public void methodWithoutArgs() {
        System.out.println(deco.bannerize("Running method without args", 50));
    }

    @Logged()
    public void methodWithTime() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Logged(time = false)
    public void methodWithoutTime() {
        for (int i = 0; i < 1000; i++) {
            //noinspection ResultOfMethodCallIgnored
            Math.sqrt(i);
        }
    }

    @Logged(returnValue = Logged.Values.ALL)
    public String methodReturnsNull() {
        return null;
    }

    @Logged(returnValue = Logged.Values.NONE)
    public String methodWithoutReturnLogging() {
        return deco.bannerize("This value should be hidden in logs", 50);
    }

    @Logged(returnValue = Logged.Values.NULL)
    public String methodReturnsNullOnlyWhenNull() {
        return null;
    }

    @Logged(returnValue = Logged.Values.NULL)
    public String methodReturnsNonNullSuppressed() {
        return deco.bannerize("You shouldn't see this", 50);
    }

    @Logged(argValues = Logged.Values.NULL)
    public void methodWithNullArgValues(String nonNull, String maybeNull) {
        if (maybeNull == null) {
            System.out.println(deco.bannerize("Received a null parameter" + nonNull,50));
        }
    }

    @Logged
    public void methodThatThrows() {
        throw new RuntimeException("kaboom");
    }

    @Logged(logStackTrace = true)
    public void methodThatThrowsWithStacktrace() {
        throw new RuntimeException("boom");
    }

    @Logged(onException = false)
    public void methodThatThrowsNoLogging() {
        throw new RuntimeException("silent fail");
    }

    @Logged(callMsg = "🧪 calling method", returnMsg = "✅ method done")
    public String methodWithCustomMessages() {
        return "Custom done";
    }

    @Logged(argValues = Logged.Values.NONE)
    public void methodWithArgValuesNone(String stringArgument) {
        System.out.println(deco.bannerize("Arg value is ignored: " + stringArgument,50));
    }

    @Logged
    public String methodWithDefaults(String stringArgument) {
        return stringArgument;
    }


    @Logged(callMsg = "Entering {m} at class {c}!")
    public String customOnCall() {
        return "custom";
    }

    @Logged(returnMsg = "Returned from method {m} in {d} milliseconds!!!")
    public String customOnReturn() {
        return "custom";
    }

    @Logged(exceptionMsg = "Something bad happened: {eM}")
    public String customExceptionMsg() {
        throw new RuntimeException("oh no");
    }

    @Logged(redactArgValues = {"arg1"}, redactAtPos = {2}, returnValue = Logged.Values.NONE)
    public String methodWithRedactedArgs(String arg1, String arg2, String arg3) {
        return arg1 + "::" + arg2 + "::" + arg3;
    }

    @Logged(marker = "slf4j")
    public String methodWithMarker(){
        return "Method with marker";
    }
}
