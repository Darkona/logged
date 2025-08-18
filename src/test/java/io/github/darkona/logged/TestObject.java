package io.github.darkona.logged;

import org.springframework.stereotype.Component;

@SuppressWarnings("UnusedReturnValue")
@Component
public class TestObject {

    @Logged
    public void methodWithArgs(String str, int num) {
        System.out.println("Processing string: " + str + ", number: " + num);
    }

    @Logged(args = false)
    public void methodWithoutArgs() {
        System.out.println("Running method without args");
    }

    @Logged(time = true)
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
            Math.sqrt(i);
        }
    }

    @Logged(returnValue = Logged.Values.ALL)
    public String methodReturnsNull() {
        return null;
    }

    @Logged(returnValue = Logged.Values.NONE)
    public String methodWithoutReturnLogging() {
        return "This value should be hidden in logs";
    }

    @Logged(returnValue = Logged.Values.NULL)
    public String methodReturnsNullOnlyWhenNull() {
        return null;
    }

    @Logged(returnValue = Logged.Values.NULL)
    public String methodReturnsNonNullSuppressed() {
        return "You shouldn't see this";
    }

    @Logged(argValues = Logged.Values.NULL)
    public void methodWithNullArgValues(String nonNull, String maybeNull) {
        if (maybeNull == null) {
            System.out.println("Received a null parameter");
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
        System.out.println("Arg value is ignored: " + stringArgument);
    }

    @Logged
    public String methodWithDefaults(String stringArgument) {
        return "default";
    }


    @Logged(callMsg = "Entering {m} at class {c}!")
    public String customOnCall(){
        return "custom";
    }

    @Logged(returnMsg = "Returned from method {m} in {d} milliseconds!!!")
    public String customOnReturn() {
        return "custom";
    }

    @Logged(exceptionMsg = "Something bad happened: {eM}")
    public String customExceptionMsg(){
        throw new RuntimeException("oh no");
    }

    @Logged(redactArgValues = {"arg1"}, redactAtPos = {2}, returnValue = Logged.Values.NONE)
    public String methodWithRedactedArgs(String arg1, String arg2, String arg3){
        return arg1 + "::" + arg2 + "::" + arg3;
    }
}
