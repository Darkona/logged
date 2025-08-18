package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.LoggedProperties;
import io.github.darkona.logged.internals.LogDecorator;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Slf4jPluginConfig {

    @Bean
    @ConditionalOnClass(LoggerFactory.class)
    @ConditionalOnMissingBean
    @ConditionalOnBooleanProperty(name = "logged.slf4j.enabled", matchIfMissing = true)
    public LoggedSlf4jPlugin loggedSlf4jPlugin(LoggedProperties props, LogDecorator decorator) {
        return new LoggedSlf4jPlugin(props, decorator);

    }
}
