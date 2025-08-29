package io.github.darkona.logged.plugins.log4j2;

import ch.qos.logback.core.spi.FilterReply;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.core.Filter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "logged.log4j2")
@Data
public class LoggedLog4j2PluginProperties {


    /**
     * Enable Log4j plugin
     */
    private boolean enabled = false;


    /**
     * List of appender/marker filter bindings.
     * Each entry defines how a log event with a specific key/value
     * should be routed to a named appender.
     */
    private List<LoggedLog4j2PluginProperties.AppenderSimpleFilter> appenderFilters = new ArrayList<>();

    /**
     * Binding configuration between a logback appender and a marker.
     */
    public static class AppenderSimpleFilter {

        /**
         * Name of the target Logback appender (e.g. "CONSOLE", "FILE", "ASYNC_CONSOLE", "ROOT").
         */
        @Getter
        @Setter
        private String appenderName;

        /**
         * Marker name to evaluate to trigger the filter
         */
        @Getter @Setter private String name;

        /**
         * Filter result when the key/value matches.
         * Defaults to {@link FilterReply#NEUTRAL}.
         */
        @Getter @Setter private Filter.Result onMatch = Filter.Result.NEUTRAL;
        /**
         * Filter result when the key/value does not match.
         * Defaults to {@link FilterReply#NEUTRAL}.
         */
        @Getter @Setter private Filter.Result onMismatch = Filter.Result.NEUTRAL;

    }
}
