package io.github.darkona.logged;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the @Logged annotation system.
 */
@Data
@ConfigurationProperties(prefix = "logged")
public class LoggedProperties {

    /**
     * Maximum length of characters for values (args and return).
     * This is an early truncation so the truncated string will be the only one available to plugins.
     */
    int maxValueLength = 2048;
    /**
     * Enable Logged
     */
    private boolean enabled = true;

    /**
     * Print log statement announcing when Logged and its plugins are loaded;
     */
    private boolean announceLoad = true;
    /**
     * Enable color log decorator.
     */
    private boolean color = true;
    /**
     * Enable symbol "icons" to Logged logs: "↑○"
     */
    private boolean icons = true;
    /**
     * Enable Utf8 in `System.out` for pretty character use.
     */
    private boolean useUtf8 = true;
    /**
     * Character to use as mask when using redact.
     */
    private String redactMask = "█";
    /**
     * Length of the redacted string that appears instead of the actual value.
     */
    private Integer redactLength = 5;
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
     * Icon to represent call depth.
     */
    private String depthIcon = ">";
}
