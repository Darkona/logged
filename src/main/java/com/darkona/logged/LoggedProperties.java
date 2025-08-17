package com.darkona.logged;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the @Logged annotation system.
 */
@ConfigurationProperties(prefix = "logged")
public class LoggedProperties {

    /**
     * Enable Logged
     */
    private Boolean enabled = true;

    /**
     * Use color in Logged logs.
     */
    private Boolean color = true;

    /**
     * Enable symbol "icons" to Logged logs: "↑○"
     */
    private Boolean icons = true;

    /**
     * Enable Utf8 in `System.out` for pretty character use.
     */
    private Boolean useUtf8 = true;

    /**
     * Icon to represent a log statement from the entry into a method.
     */
    private String entryIcon = "↓○";

    /**
     * Icon to represent a log statement from the exit of a method.
     */
    private String exitIcon = "↑○";

    /**
     * Icon to represent a log statement from an exception in the method.
     */
    private String throwIcon = "↑x";

    /**
     * Template for method entry when there are no arguments.
     * <p>Default: {@code "{eI}{c}::{m} called."}</p>
     * <p>Meaning: Logs the method entry using the entry icon, class name, and method name.</p>
     * <p>Example: {@code ↓○MyClass::myMethod called.}</p>
     */
    private String callMsgNoArgs = "{eI}{c}::{m} called.";

    /**
     * Template for method entry when there are arguments.
     * <p>Default: {@code "{eI}{c}::{m} called with args: [{a}]"} </p>
     * <p>Meaning: Logs method entry and includes the argument list.</p>
     * <p>Example: {@code ↓○MyClass::myMethod called with args: [42, "foo"]}</p>
     */
    private String callMsgArgs = "{eI}{c}::{m} called with args: [{a}]";

    /**
     * Template for method exit without a return value.
     * <p>Default: {@code "{xI}{c}::{m} returned."}</p>
     * <p>Meaning: Logs the fact that a method has returned, without showing any return value.</p>
     * <p>Example: {@code ↑○MyClass::myMethod returned.}</p>
     */
    private String exitMsg = "{xI}{c}::{m} returned.";

    /**
     * Template for arguments or parameters of the method.
     * The only tokens for substitution that work here are {c} for class name, {k} for the parameter name and {v} for the value.
     * <p>Default: {@code "({c}) {k}={v}"}</p>
     * <p>Meaning: Logs argument type, name and value</p>
     */
    private String argsTemplate = "({c}) {k}={v}";
    /**
     * Template for method exit with a return value.
     * <p>Default: {@code "{xI}{c}::{m} returned with value: {rV}"}</p>
     * <p>Meaning: Logs that the method returned and shows the return value.</p>
     * <p>Example: {@code ↑○MyClass::myMethod returned with value: 123}</p>
     */
    private String exitMsgValue = "{xI}{c}::{m} returned with value: {rV}";

    /**
     * Template for logging method execution time.
     * <p>Default: {@code "Time taken: {d} ms"}</p>
     * <p>Meaning: Logs how long the method took to execute, in milliseconds.</p>
     * <p>Example: {@code Time taken: 42 ms}</p>
     */
    private String timeTakenMsg = "Time taken: {d} ms";

    /**
     * Template for exception thrown during method execution.
     * <p>Default: {@code "{tI}{c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})"}</p>
     * <p>Meaning: Logs the exception with its type, message, and origin point in the code.</p>
     * <p>Example: {@code ↑xMyClass::myMethod threw a NullPointerException: boom at MyClass.otherMethod (MyClass.java:42)}</p>
     */
    private String throwMsg = "{tI}{c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})";

    /**
     * Character to use as mask when using redact.
     */
    private Character redactMask = '█';

    /**
     * Length of the redacted string that appears instead of the actual value.
     */
    private Integer redactLength = 5;

    /**
     * Inject data from Logged into OpenTelemetry spans.
     */
    private Boolean useOTel = true;

    public Boolean getUseUtf8() {
        return useUtf8;
    }

    public void setUseUtf8(Boolean useUtf8) {
        this.useUtf8 = useUtf8;
    }

    public Boolean getColor() {
        return color;
    }

    public void setColor(Boolean color) {
        this.color = color;
    }

    public Boolean getIcons() {
        return icons;
    }

    public void setIcons(Boolean icons) {
        this.icons = icons;
    }

    public String getEntryIcon() {
        return entryIcon;
    }

    public void setEntryIcon(String entryIcon) {
        this.entryIcon = entryIcon;
    }

    public String getExitIcon() {
        return exitIcon;
    }

    public void setExitIcon(String exitIcon) {
        this.exitIcon = exitIcon;
    }

    public String getThrowIcon() {
        return throwIcon;
    }

    public void setThrowIcon(String throwIcon) {
        this.throwIcon = throwIcon;
    }

    public String getCallMsgNoArgs() {
        return callMsgNoArgs;
    }

    public void setCallMsgNoArgs(String callMsgNoArgs) {
        this.callMsgNoArgs = callMsgNoArgs;
    }

    public String getCallMsgArgs() {
        return callMsgArgs;
    }

    public void setCallMsgArgs(String callMsgArgs) {
        this.callMsgArgs = callMsgArgs;
    }

    public String getExitMsg() {
        return exitMsg;
    }

    public void setExitMsg(String exitMsg) {
        this.exitMsg = exitMsg;
    }

    public String getExitMsgValue() {
        return exitMsgValue;
    }

    public void setExitMsgValue(String exitMsgValue) {
        this.exitMsgValue = exitMsgValue;
    }

    public String getTimeTakenMsg() {
        return timeTakenMsg;
    }

    public void setTimeTakenMsg(String timeTakenMsg) {
        this.timeTakenMsg = timeTakenMsg;
    }

    public String getThrowMsg() {
        return throwMsg;
    }

    public void setThrowMsg(String throwMsg) {
        this.throwMsg = throwMsg;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Character getRedactMask() {
        return redactMask;
    }

    public  void setRedactMask(Character redactMask) {
        this.redactMask = redactMask;
    }

    public Integer getRedactLength(){
        return redactLength;
    }

    public void setRedactLength(Integer redactLength){
        this.redactLength = redactLength;
    }

    public String getArgsTemplate() {
        return argsTemplate;
    }

    public void setArgsTemplate(String argsTemplate) {
        this.argsTemplate = argsTemplate;
    }
}
