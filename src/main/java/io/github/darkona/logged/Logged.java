package io.github.darkona.logged;


import org.slf4j.event.Level;

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
    String callMsg() default "";

    /**
     * Print a custom message on this method's return.
     */
    String returnMsg() default "";

    /**
     * Print a custom message if an exception occurs.
     */
    String exceptionMsg() default "";

    /**
     * Log level for this method's logs.
     */
    Level level() default Level.INFO;

    /**
     * Log level for exceptions
     */
    Level exceptionLevel() default Level.ERROR;

    /**
     * Print the stack trace when an exception occurs.
     */
    boolean logStackTrace() default false;

    /**
     * Redact parameter value by name of parameter
     */
    String[] redactArgValues() default {};

    /**
     * Redact parameter value by position of parameter (0 is first parameter)
     */
    int[] redactAtPos() default {};

    /**
     * Add a marker to the log so it can be filtered later by a logging appender
     */
    String marker() default "";

    /**
     * NONE = No values are printed.
     * ALL= All the values are printed.
     * NULL= Only print if the value is null (helps identify values incorrectly set as null, without printing the non-null values).
     */
    enum Values {
        NONE, ALL, NULL
    }

}
