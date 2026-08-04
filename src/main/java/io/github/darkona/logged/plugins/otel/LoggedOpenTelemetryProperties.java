package io.github.darkona.logged.plugins.otel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logged.otel")
@Data
public class LoggedOpenTelemetryProperties {

    /**
     * Enable the plugin
     */
    private boolean enabled = false;

    /**
     * Add the line in the source file where the method call occurs to the span attributes
     */
    private boolean addSourceLine = true;

    /**
     * Add the class name to the span attributes.
     */
    private boolean addClass = true;

    /**
     * Add the method name to the span attributes
     */
    private boolean addMethod = true;

    /**
     * Add the call stack depth to the span attributes
     */
    private boolean addDepth = true;

    /**
     * Add the method's arguments to the span attributes
     */
    private boolean addArgs = true;

    /**
     * Add the method's return type to the span attributes
     */
    private boolean addReturnType = true;

    /**
     * Add the exception message to the span attributes
     */
    private boolean addExceptionMsg = true;

    /**
     * Add the span name to the MDC
     */
    private boolean addToMdc = true;

    /**
     * Key for the mdc span name
     */
    private String mdcKey = "span_name";
    /**
     * The template to follow for the span name
     */
    private String spanIdTemplate = "Logged: {c}#{m}()";
}
