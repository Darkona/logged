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

    /** Enable/disable the library. */
    private boolean enabled = true;

    /** Announce when Logged and its plugins are loaded. */
    private boolean announceLoad = true;

    /** Enable color decorator. */
    private boolean color = true;

    /** Enable symbol icons in logs. */
    private boolean icons = true;

    /** Enable UTF-8 for System.out to support pretty symbols. */
    private boolean useUtf8 = true;

    /** String to use for masking sensitive values. */
    private String maskString = "█";

    /** Length of the mask shown instead of actual value. */
    private Integer maskLength = 5;

    /** Mask values whose String representation matches these regex patterns. */
    private java.util.List<String> maskPatterns = java.util.Collections.emptyList();

    /** Mask values whose runtime type matches any of these class names (FQCN). */
    private java.util.List<String> maskTypeNames = java.util.Collections.emptyList();

    /** Fail startup on invalid maskPatterns. */
    private boolean failOnInvalidMaskPatterns = false;

    /** Fail startup on unresolved maskTypeNames. */
    private boolean failOnUnresolvedMaskTypes = false;

    /** Mask return values globally unless overridden at annotation level. */
    private boolean maskReturn = false;

    /** Icons (ASCII by default). */
    private String callIcon = ">>";
    private String returnIcon = "<<";
    private String exceptionIcon = "!!";
    private String depthIcon = ">";

    /** Use a predefined icon theme when true. */
    private boolean useIconTheme = false;

    /** Optional icon theme preset (used when useIconTheme = true). */
    private SymbolTheme iconTheme;

    /** Threshold configuration for slow calls. */
    private Threshold threshold = new Threshold();

    @Data
    public static class Threshold {
        /** Global threshold in milliseconds. If < 0 the feature is disabled globally. */
        private long warnMs = -1L;
        /** Level to promote to when threshold is exceeded. */
        private org.slf4j.event.Level promoteLevel = org.slf4j.event.Level.WARN;
    }
}
