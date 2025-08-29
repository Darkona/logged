package io.github.darkona.logged;

import io.github.darkona.logged.utils.SymbolTheme;
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
     * Redact values whose String representation matches any of these regular expressions.
     */
    private java.util.List<String> redactPatterns = java.util.Collections.emptyList();

    /**
     * Redact values whose runtime type is assignable to any of these class names.
     * Specify fully-qualified class names. Classes are resolved lazily at runtime.
     */
    private java.util.List<String> redactTypeNames = java.util.Collections.emptyList();
    /**
     * If true, application startup fails when a pattern in {@code logged.redactPatterns}
     * cannot be compiled as a valid regular expression. When false, invalid
     * patterns are ignored with a warning.
     */
    private boolean failOnInvalidRedactPatterns = false;
    /**
     * If true, application startup fails when a class name in {@code logged.redactTypeNames}
     * cannot be resolved. When false, unresolved classes are ignored with a warning.
     */
    private boolean failOnUnresolvedRedactTypes = false;

    /**
     * If true, mask return values globally unless overridden at the annotation level.
     */
    private boolean maskReturn = false;
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

    /**
     * When true and {@link #iconTheme} is set, icons will be sourced from the selected
     * {@link SymbolTheme}. When false, the explicit icon strings above are used.
     */
    private boolean useIconTheme = false;

    /**
     * Optional icon theme preset. Only used when {@link #useIconTheme} is true.
     */
    private SymbolTheme iconTheme;

    /**
     * Threshold configuration for promoting slow calls.
     * Usage in YAML:
     *
     * logged:
     *   threshold:
     *     warnMs: 250
     *     promoteLevel: WARN
     */
    private Threshold threshold = new Threshold();

    @Data
    public static class Threshold {
        /**
         * Global threshold in milliseconds. If < 0 the feature is disabled globally.
         */
        private long warnMs = -1L;
        /**
         * Level to promote to when threshold is exceeded. The effective level will be the
         * highest of the base level and this value. Only used when warnMs >= 0.
         */
        private org.slf4j.event.Level promoteLevel = org.slf4j.event.Level.WARN;
    }
}
