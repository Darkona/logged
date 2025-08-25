package io.github.darkona.logged.plugins.otel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logged.otel")
@Data
public class LoggedOpenTelemetryProperties {

    private Boolean enabled = false;

    private Boolean addSourceLine = true;

    private Boolean addClass = true;

    private Boolean addMethod = true;

    private Boolean addDepth = true;

    private Boolean addArgs = true;

    private Boolean addReturnType = true;

    private Boolean addExceptionMsg = true;
}
