package io.github.darkona.logged.plugins.mdc;

import io.github.darkona.logged.api.LogDecorator;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;


@AutoConfiguration
@EnableConfigurationProperties(LoggedMdcProperties.class)
public class LoggedMdcPluginConfig {

    @Bean
    @ConditionalOnClass(MDC.class)
    @ConditionalOnBooleanProperty(name = "logged.mdc.enabled")
    @Order(100)
    public LoggedMdcPlugin mdcPlugin(LogDecorator logDecorator, LoggedMdcProperties props) {
        return new LoggedMdcPlugin(logDecorator, props);
    }

}
