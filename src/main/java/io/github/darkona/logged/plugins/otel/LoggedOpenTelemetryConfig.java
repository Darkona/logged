package io.github.darkona.logged.plugins.otel;

import io.github.darkona.logged.api.LogDecorator;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.util.Optional;

@AutoConfiguration
@ConditionalOnClass(name = "io.opentelemetry.api.trace.Tracer")
@EnableConfigurationProperties({LoggedOpenTelemetryProperties.class})
public class LoggedOpenTelemetryConfig {

    @Bean(name = "loggedTracer")
    @ConditionalOnMissingBean(name = "loggedTracer")
    @ConditionalOnBooleanProperty(name = "logged.oTel.enabled")
    public Tracer tracer(ObjectProvider<OpenTelemetry> otelProvider) {

        OpenTelemetry oTel = otelProvider.getIfAvailable(GlobalOpenTelemetry::get);
        var scopeName = "io.github.darkona.logged";
        String scopeVersion = Optional.ofNullable(LoggedOpenTelemetryPlugin.class.getPackage().getImplementationVersion()).orElse("dev");
        return oTel.getTracer(scopeName, scopeVersion);
    }

    @Bean
    @ConditionalOnClass({Span.class, Tracer.class})
    @ConditionalOnBean(name = "loggedTracer")
    @ConditionalOnBooleanProperty(name = "logged.oTel.enabled")
    @Order(0)
    public LoggedOpenTelemetryPlugin openTelemetryPlugin(LogDecorator logDecorator, LoggedOpenTelemetryProperties props, Tracer tracer) {
        return new LoggedOpenTelemetryPlugin(logDecorator, props, tracer);
    }

}
