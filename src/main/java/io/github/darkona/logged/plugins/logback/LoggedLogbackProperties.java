package io.github.darkona.logged.plugins.logback;

import ch.qos.logback.core.spi.FilterReply;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;


/**
 * Configuration properties for customizing Logback appenders using markers.
 * <p>
 * This class is bound from YAML/Properties files using the prefix
 * <code>logged.logback</code>. It allows routing logs to different appenders
 * based on a key/value pair (Marker) with optional
 * async handling and configurable match behavior.
 *
 * <p>Example (YAML):
 * <pre>
 * logged:
 *   logback:
 *     appender-markers:
 *       - appender-name: CONSOLE
 *         appender-async-name: CONSOLE_ASYNC
 *         key: marker
 *         value: LOKI
 *         async: true
 *         on-match: ACCEPT
 *         on-mismatch: DENY
 * </pre>
 *
 * @author Darkona
 */
@Data
@ConfigurationProperties(prefix = "logged.logback")
public class LoggedLogbackProperties {

    /**
     * Enable Logback filter plugin
     */
    private boolean enabled = false;

    /**
     * List of appender/marker filter bindings.
     * Each entry defines how a log event with a specific key/value
     * should be routed to a named appender.
     */
    private List<AppenderSimpleFilter> appenderFilters = new ArrayList<>();

    /**
     * Binding configuration between a logback appender and a marker.
     */
    public static class AppenderSimpleFilter {

        /**
         * Name of the target Logback appender (e.g. "CONSOLE", "FILE", "ASYNC_CONSOLE", "ROOT").
         */
        @Getter @Setter private String appenderName;

        /**
         * Marker name to evaluate to trigger the filter
         */
        @Getter @Setter private String name;

        /**
         * Filter result when the key/value matches.
         * Defaults to {@link FilterReply#NEUTRAL}.
         */
        @Getter @Setter private FilterReply onMatch = FilterReply.NEUTRAL;
        /**
         * Filter result when the key/value does not match.
         * Defaults to {@link FilterReply#NEUTRAL}.
         */
        @Getter @Setter private FilterReply onMismatch = FilterReply.NEUTRAL;

    }
}
