package com.darkona.logged;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.darkona.logged.Constants.ERROR;
import static com.darkona.logged.Constants.INFO;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD})
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
    String level() default INFO;

    /**
     * Log level for exceptions
     */
    String exceptionLevel() default ERROR;

    /**
     * Print the stack trace when an exception occurs.
     */
    boolean logStackTrace() default false;

    /**
     * NONE = No values are printed.
     * ALL= All the values are printed.
     * NULL= Only print if the value is null (helps identify values incorrectly set as null, without printing the non-null values).
     */
    enum Values {
        NONE, ALL, NULL
    }

}
