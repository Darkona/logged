package io.github.darkona.logged.plugins.logback;

import io.github.darkona.logged.api.LogDecorator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;


@AutoConfiguration
@ConditionalOnClass(name = {"ch.qos.logback.classic.Logger", "ch.qos.logback.core.ConsoleAppender"})
@EnableConfigurationProperties(LoggedLogbackProperties.class)
public class LoggedLogbackConfig {

    @Bean
    @ConditionalOnMissingBean
    @Order(250)
    @ConditionalOnBooleanProperty("logged.logback.enabled")
    public LoggedLogbackPlugin loggedLogbackPlugin(LoggedLogbackProperties loggedLogbackProperties, LogDecorator logDecorator) {
        return new LoggedLogbackPlugin(loggedLogbackProperties, logDecorator);
    }



}
