package io.github.darkona.logged;


import org.slf4j.event.Level;
import org.springframework.lang.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RUNTIME)
public @interface Logged {

    /**
     * Print a log on method call/entry
     */
    boolean onCall() default true;

    /**
     * Print the call arguments (type, name)
     */
    boolean args() default true;

    /**
     * Print the call argument values
     */
    Values argValues() default Values.ALL;

    /**
     * Print a log on method return/exit
     */
    boolean onReturn() default true;

    /**
     * Print the method's return value.
     */
    Values returnValue() default Values.ALL;

    /**
     * Print a log when an exception occurs
     */
    boolean onException() default true;

    /**
     * Print the execution time of the method on exit (or exception)
     */
    boolean time() default true;

    /**
     * Print a custom message on this method's call.
     */
    @NonNull String callMsg() default "";

    /**
     * Print a custom message on this method's return.
     */
    @NonNull String returnMsg() default "";

    /**
     * Print a custom message if an exception occurs.
     */
    @NonNull String exceptionMsg() default "";

    /**
     * If >= 0, when the measured execution time (in milliseconds) exceeds this threshold
     * the log level may be promoted (see global configuration) and an optional marker
     * from {@link #slowMarker()} will be added to the log event.
     * A value of -1 disables per-method threshold (falls back to global setting).
     */
    long warnIfOverMs() default -1L;

    /**
     * Optional marker to add to the log event when the call exceeds the configured threshold.
     * Ignored if no threshold is active or if the call does not exceed it.
     */
    @NonNull String slowMarker() default "";

    /**
     * Log level for this method's logs.
     */
    @NonNull Level level() default Level.INFO;

    /**
     * Log level for exceptions
     */
    @NonNull Level exceptionLevel() default Level.ERROR;

    /**
     * Print the stack trace when an exception occurs.
     */
    boolean logStackTrace() default false;

    /**
     * Mask parameter value by the parameter name.
     */
    @NonNull String[] maskArgValues() default {};

    /**
     * Mask parameter value by parameter position (0 is first parameter).
     */
    int[] maskAtPos() default {};

    /**
     * Mask parameter values when their runtime type matches any of these classes
     * (or is assignable to them). Additive with name/position masking.
     */
    @NonNull Class<?>[] maskTypes() default {};

    /**
     * Mask parameter values when their String representation matches any of these
     * regular expressions. Additive with name/position masking.
     */
    @NonNull String[] maskPatterns() default {};

    /**
     * If true, mask the method's return value in logs. Can also be enabled globally
     * via properties. Type/pattern masking also applies to return values.
     */
    boolean maskReturn() default false;

    /**
     * Add a marker to the log so it can be filtered later by a logging appender
     */
    @NonNull String[] markers() default {};

    /**
     * NONE = No values are printed.
     * ALL= All the values are printed.
     * NULL= Only print if the value is null (helps identify values incorrectly set as null, without printing the non-null values).
     */
    enum Values {
        NONE, ALL, NULL
    }

}
