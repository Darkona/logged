package com.darkona.logged;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logged")
public class LoggedProperties {

    private Boolean color = true;
    private Boolean icons = true;
    private String entryIcon = "↓○";
    private String exitIcon = "↑○";
    private String throwIcon = "↑x";
    private String callMsgNoArgs = "{eI}{c}::{m} called.";
    private String callMsgArgs = "{eI}{c}::{m} called with args: [{a}]";
    private String exitMsg = "{xI}{c}::{m} returned.";
    private String exitMsgValue = "{xI}{c}::{m} returned with value: {rV}";
    private String timeTakenMsg = "Time taken: {d} ms";
    private String throwMsg = "{tI}{c}::{m} threw a {ex}: {eM} \n\tat {ec}.{em} ({f}:{L})";

    String getTimeTakenMsg() {
        return timeTakenMsg;
    }

    void setTimeTakenMsg(String timeTakenMsg) {
        this.timeTakenMsg = timeTakenMsg;
    }

    String getExitMsgValue() {
        return exitMsgValue;
    }

    void setExitMsgValue(String exitMsgValue) {
        this.exitMsgValue = exitMsgValue;
    }

    String getCallMsgNoArgs() {
        return callMsgNoArgs;
    }

    void setCallMsgNoArgs(String callMsgNoArgs) {
        this.callMsgNoArgs = callMsgNoArgs;
    }

    String getCallMsgArgs() {
        return callMsgArgs;
    }

    void setCallMsgArgs(String callMsgArgs) {
        this.callMsgArgs = callMsgArgs;
    }

    String getExitMsg() {
        return exitMsg;
    }

    void setExitMsg(String exitMsg) {
        this.exitMsg = exitMsg;
    }

    String getThrowMsg() {
        return throwMsg;
    }

    void setThrowMsg(String throwMsg) {
        this.throwMsg = throwMsg;
    }

    public void setIcons(final Boolean icons) {
        this.icons = icons;
    }
    public Boolean getIcons() {
        return icons;
    }

    String getEntryIcon() {
        return entryIcon;
    }

    void setEntryIcon(String entryIcon) {
        this.entryIcon = entryIcon;
    }

    String getExitIcon() {
        return exitIcon;
    }

    void setExitIcon(String exitIcon) {
        this.exitIcon = exitIcon;
    }

    String getThrowIcon() {
        return throwIcon;
    }

    void setThrowIcon(String throwIcon) {
        this.throwIcon = throwIcon;
    }

    Boolean getColor() {
        return color;
    }

    void setColor(Boolean color) {
        this.color = color;
    }
}
