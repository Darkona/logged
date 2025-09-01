package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.api.LogToken;
import io.github.darkona.logged.colors.BasicColor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "logged.slf4j")
@Data
public class LoggedSlf4jProperties {

    /**
     * Enable Logged Slf4j Plugin
     */
    private boolean enabled = true;

    /**
     * Use color in Logged logs.
     */
    private boolean color = true;

    /**
     * Use Icon colors
     */
    private boolean iconColors = true;

    /**
     * Template for method entry when there are no arguments.
     * <p>Default: {@code "{eI} {c}::{m} called."}</p>
     * <p>Meaning: Logs the method entry using the entry icon, class name, and method name.</p>
     * <p>Example: {@code ↓○ MyClass::myMethod called.}</p>
     */
    private String callMsgNoArgs = "{h:}{cI:} {c}::{m} called.";

    /**
     * Template for method entry when there are arguments.
     * <p>Default: {@code "{eI} {c}::{m} called with args: [{a}]"} </p>
     * <p>Meaning: Logs method entry and includes the argument list.</p>
     * <p>Example: {@code ↓○ MyClass::myMethod called with args: [42, "foo"]}</p>
     */
    private String callMsgArgs = "{h:}{cI:} {c}::{m} called with args: [{a}]";

    /**
     * Template for method exit without a return value.
     * <p>Default: {@code "{xI} {c}::{m} returned."}</p>
     * <p>Meaning: Logs the fact that a method has returned, without showing any return value.</p>
     * <p>Example: {@code ↑○ MyClass::myMethod returned.}</p>
     */
    private String returnMsg = "{h:}{rI:} {c}::{m} returned.";

    /**
     * Template for arguments or parameters of the method.
     * The only tokens for substitution that work here are {c} for class name, {k} for the parameter name and {v} for the value.
     * <p>Default: {@code "({c}) {k}={v}"}</p>
     * <p>Meaning: Logs argument type, name and value</p>
     */
    private String argsTemplate = "({c}){k}:{v}";

    /**
     * Template for method exit with a return value.
     * <p>Default: {@code "{xI} {c}::{m} returned with value: {rV}"}</p>
     * <p>Meaning: Logs that the method returned and shows the return value.</p>
     * <p>Example: {@code ↑○ MyClass::myMethod returned with value: 123}</p>
     */
    private String returnMsgValue = "{h:}{rI:} {c}::{m} returned with value: {rV}";


    /**
     * Template for exception thrown during method execution.
     * <p>Default: {@code "{tI} {c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})"}</p>
     * <p>Meaning: Logs the exception with its type, message, and origin point in the code.</p>
     * <p>Example: {@code ↑x MyClass::myMethod threw a NullPointerException: boom at MyClass.otherMethod (MyClass.java:42)}</p>
     */
    private String exceptionMsg = "{h:}{exI:} {c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})";

    /**
     * Template for logging method execution time.
     * <p>Default: {@code "Time taken: {d} ms"}</p>
     * <p>Meaning: Logs how long the method took to execute, in milliseconds.</p>
     * <p>Example: {@code Time taken: 42 ms}</p>
     */
    private String timeTakenMsg = "Time taken: {d} ms";

    /**
     * Add a depth icon and move call statements to the right for each subsequent call in the stack.
     */
    private boolean logDepth = true;

    /**
     * List of keys to try to capture from MDC to add to logs.
     * Add the keys separated by commas, example: trace_id, span_id, call_id
     * Use {mdc.trace_id} at a log message to print the value with a key 'trace_id'
     */
    private List<String> captureFromMdc = new ArrayList<>();

    /**
     * Add marker to logs emitted by Slf4j
     */
    private String[] markers = {};

    /**
     * Maximum amount of characters allowed when parsing values.
     */
    private int maxValueLength = 2048;

    /**
     *
     */
    private List<String> captureFromOtel = new ArrayList<>();


    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors.Blue} for reference;
     */
    private String callIconColor = "BLUE";

    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors.Green} for reference;
     */
    private String returnIconColor = "GREEN";

    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors.Red} for reference;
     */
    private String exceptionIconColor = "RED";

    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors.Orange} for reference;
     */
    private String depthIconColor = "ORANGE";

    /**
     * Add values from Logged as key values in the logging event.
     * Allows using a different message and the logging library interpolation instead or together with Logged's
     */
    boolean keyValue = false;

    /**
     * List of Logged-capture data tokens to add as key-value attributes in the log event
     */
    List<LogToken> keyValues = List.of(
            LogToken.ARGUMENTS,
            LogToken.METHOD_NAME,
            LogToken.METHOD_TYPE,
            LogToken.RETURN_VALUE,
            LogToken.DURATION);
}
