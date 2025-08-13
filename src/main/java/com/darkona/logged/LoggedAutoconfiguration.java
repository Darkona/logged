package com.darkona.logged;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnMissingBean(LoggedAutoconfiguration.class)
@ConditionalOnClass({LoggedAspect.class, Logged.class, Logger.class, LogDecorator.class, LoggedProperties.class})
@EnableConfigurationProperties({LoggedProperties.class})
public class LoggedAutoconfiguration {


    @Bean
    @ConditionalOnBooleanProperty(value = "logged.color")
    public LogDecorator colorLogDecorator() {
        return new ColorLogDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(LogDecorator.class)
    public LogDecorator plainLogDecorator() {
        return new PlainLogDecorator();
    }

    @Bean
    public LoggedAspect loggedAspect(LogDecorator logDecorator, LoggedProperties loggedProperties) {
        return new LoggedAspect(loggedProperties, logDecorator);
    }

}
