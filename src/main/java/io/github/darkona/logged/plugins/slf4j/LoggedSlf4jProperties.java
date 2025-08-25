package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.colors.BasicColor;
import io.github.darkona.logged.colors.ColorEnum;
import io.github.darkona.logged.colors.ColorFinder;
import io.github.darkona.logged.colors.Orange;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "logged.slf4j")
public class LoggedSlf4jProperties {

    /**
     * Enable Logged Slf4j Plugin
     */
    @Getter
    @Setter
    private boolean enabled = true;

    /**
     * Use color in Logged logs.
     */
    @Getter
    @Setter
    private boolean color = true;

    @Getter
    @Setter
    private boolean iconColors = true;


    /**
     * Template for method entry when there are no arguments.
     * <p>Default: {@code "{eI} {c}::{m} called."}</p>
     * <p>Meaning: Logs the method entry using the entry icon, class name, and method name.</p>
     * <p>Example: {@code ↓○ MyClass::myMethod called.}</p>
     */
    @Getter
    @Setter
    private String callMsgNoArgs = "{h:}{eI:} {c}::{m} called.";

    /**
     * Template for method entry when there are arguments.
     * <p>Default: {@code "{eI} {c}::{m} called with args: [{a}]"} </p>
     * <p>Meaning: Logs method entry and includes the argument list.</p>
     * <p>Example: {@code ↓○ MyClass::myMethod called with args: [42, "foo"]}</p>
     */
    @Getter
    @Setter
    private String callMsgArgs = "{h:}{eI:} {c}::{m} called with args: [{a}]";

    /**
     * Template for method exit without a return value.
     * <p>Default: {@code "{xI} {c}::{m} returned."}</p>
     * <p>Meaning: Logs the fact that a method has returned, without showing any return value.</p>
     * <p>Example: {@code ↑○ MyClass::myMethod returned.}</p>
     */
    @Getter
    @Setter
    private String exitMsg = "{h:}{xI:} {c}::{m} returned.";

    /**
     * Template for arguments or parameters of the method.
     * The only tokens for substitution that work here are {c} for class name, {k} for the parameter name and {v} for the value.
     * <p>Default: {@code "({c}) {k}={v}"}</p>
     * <p>Meaning: Logs argument type, name and value</p>
     */
    @Getter
    @Setter
    private String argsTemplate = "({c}) {k}={v}";

    /**
     * Template for method exit with a return value.
     * <p>Default: {@code "{xI} {c}::{m} returned with value: {rV}"}</p>
     * <p>Meaning: Logs that the method returned and shows the return value.</p>
     * <p>Example: {@code ↑○ MyClass::myMethod returned with value: 123}</p>
     */
    @Getter
    @Setter
    private String exitMsgValue = "{h:}{xI:} {c}::{m} returned with value: {rV}";


    /**
     * Template for exception thrown during method execution.
     * <p>Default: {@code "{tI} {c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})"}</p>
     * <p>Meaning: Logs the exception with its type, message, and origin point in the code.</p>
     * <p>Example: {@code ↑x MyClass::myMethod threw a NullPointerException: boom at MyClass.otherMethod (MyClass.java:42)}</p>
     */
    @Getter
    @Setter
    private String throwMsg = "{h:}{tI:} {c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})";

    /**
     * Template for logging method execution time.
     * <p>Default: {@code "Time taken: {d} ms"}</p>
     * <p>Meaning: Logs how long the method took to execute, in milliseconds.</p>
     * <p>Example: {@code Time taken: 42 ms}</p>
     */
    @Getter
    @Setter
    private String timeTakenMsg = "Time taken: {d} ms";

    /**
     * Add a depth icon and move call statements to the right for each subsequent call in the stack.
     */
    @Getter
    @Setter
    private boolean logDepth = true;

    /**
     * List of keys to try to capture from MDC to add to logs.
     * Add the keys separated by commas, example: trace_id, span_id, call_id
     * Use {mdc.trace_id} at a log message to print the value with a key 'trace_id'
     */
    @Getter
    @Setter
    private List<String> captureFromMdc = new ArrayList<>();


    @Getter
    @Setter
    private List<String> captureFromOtel = new ArrayList<>();
    @Getter
    private ColorEnum entryIconColor = BasicColor.BLUE;
    @Getter
    private ColorEnum exitIconColor = BasicColor.GREEN;
    @Getter
    private ColorEnum throwIconColor = BasicColor.RED;
    @Getter
    private ColorEnum depthIconColor = Orange.ORANGE;


    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors} for reference;
     */
    public void setEntryIconColor(String entryIconColor) {
        this.entryIconColor = ColorFinder.findColor(entryIconColor);
    }

    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors} for reference;
     */
    public void setExitIconColor(String exitIconColor) {
        this.exitIconColor = ColorFinder.findColor(exitIconColor);
    }

    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors} for reference;
     */
    public void setThrowIconColor(String entryIconColor) {
        throwIconColor = ColorFinder.findColor(entryIconColor);
    }

    /**
     * Uses {@link BasicColor} or any of the color classes to find a color.
     * See {@link io.github.darkona.logged.colors} for reference;
     */
    public void setDepthIconColor(String entryIconColor) {
        depthIconColor = ColorFinder.findColor(entryIconColor);
    }
}
