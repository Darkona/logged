package io.github.darkona.logged.plugins.otel;

import io.github.darkona.logged.internals.LogDecorator;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(Tracer.class)
public class OpenTelemetryConfig {

    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("logged");
    }

    @Bean
    @ConditionalOnClass(Span.class)
    @ConditionalOnBean(Tracer.class)
    @ConditionalOnBooleanProperty(name = "logged.oTel.enabled")
    public LoggedOpenTelemetryPlugin openTelemetryPlugin(LogDecorator logDecorator, Tracer tracer) {
        return new LoggedOpenTelemetryPlugin(logDecorator, tracer);
    }
}
