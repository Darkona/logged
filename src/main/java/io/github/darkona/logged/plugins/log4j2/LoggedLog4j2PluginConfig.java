package io.github.darkona.logged.plugins.log4j2;


import io.github.darkona.logged.api.LogDecorator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;


@AutoConfiguration
@ConditionalOnClass(name = {"org.apache.logging.log4j.LogManager"})
@ConditionalOnMissingClass("ch.qos.logback.classic.Logger")
@EnableConfigurationProperties(LoggedLog4j2PluginProperties.class)
public class LoggedLog4j2PluginConfig {

    @Bean
    @ConditionalOnMissingBean
    @Order(251)
    @ConditionalOnBooleanProperty("logged.log4j2.enabled")
    public LoggedLog4j2Plugin loggedLog4j2Plugin(LoggedLog4j2PluginProperties properties, LogDecorator decorator) {
        return new LoggedLog4j2Plugin(properties, decorator);
    }
}
