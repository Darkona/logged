package io.github.darkona.logged.plugins.slf4j;

import io.github.darkona.logged.api.LogDecorator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@ConditionalOnClass(name = "org.slf4j.LoggerFactory")
@EnableConfigurationProperties(LoggedSlf4jProperties.class)
public class LoggedSlf4jPluginConfig {


    @Bean
    @Order(200)
    @ConditionalOnBooleanProperty(name = "logged.slf4j.enabled", matchIfMissing = true)
    public LoggedSlf4jPlugin loggedSlf4jPlugin(LogDecorator decorator, LoggedSlf4jProperties props) {
        return new LoggedSlf4jPlugin(decorator, props);
    }
}
