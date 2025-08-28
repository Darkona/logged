package io.github.darkona.logged.plugins.logback;

import ch.qos.logback.classic.AsyncAppender;
import io.github.darkona.logged.api.LogDecorator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that wires {@link MarkerFilter} instances to Logback appenders
 * based on {@link LoggedLogbackProperties}.
 *
 * <p>This configuration reads the list of <code>appender-markers</code> from
 * {@code application.yaml} and attaches filters to the corresponding appenders
 * so that log events are routed conditionally depending on an MDC key/value pair.
 *
 * <p>Each {@link LoggedLogbackProperties.AppenderSimpleFilter} entry declares:
 * <ul>
 *   <li>{@code appender-name} – the Logback appender to attach the filter to</li>
 *   <li>{@code appender-async-name} – optional async wrapper appender name</li>
 *   <li>{@code key}/{@code value} – MDC key/value that must match</li>
 *   <li>{@code async} – whether to look for the appender inside an {@link AsyncAppender}</li>
 *   <li>{@code on-match}/{@code on-mismatch} – {@link ch.qos.logback.core.spi.FilterReply}
 *       values controlling what happens when the filter matches or not</li>
 * </ul>
 *
 * <p><b>Example configuration (application.yaml):</b>
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
 * In this example:
 * <ul>
 *   <li>Whenever a log event has {@code MDC["marker"] == "LOKI"}, it will be accepted
 *       by the {@code CONSOLE} appender.</li>
 *   <li>Otherwise, the event will be denied for that appender.</li>
 *   <li>The filter is attached to {@code CONSOLE} inside the async wrapper
 *       {@code CONSOLE_ASYNC}.</li>
 * </ul>
 *
 * <p>This allows fine-grained routing of logs to appenders (e.g. send only certain
 * markers to Loki, others to File, etc.) without modifying Logback XML directly.
 *
 * @see LoggedLogbackProperties
 * @see MarkerFilter
 */
@AutoConfiguration
@EnableConfigurationProperties(LoggedLogbackProperties.class)
public class LoggedLogbackConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBooleanProperty("logged.logback.enabled")
    public LoggedLogbackPlugin loggedLogbackPlugin(LoggedLogbackProperties loggedLogbackProperties, LogDecorator logDecorator) {
        return new LoggedLogbackPlugin(loggedLogbackProperties, logDecorator);
    }

}
