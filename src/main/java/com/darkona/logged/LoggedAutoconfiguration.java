package com.darkona.logged;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
@ConditionalOnClass({LoggedAspect.class, Logged.class, Logger.class, LogDecorator.class})
@EnableConfigurationProperties({LoggedProperties.class})
public class LoggedAutoconfiguration {


    @Bean
    @Profile({"dev", "local"})
    @ConditionalOnProperty(value = "logged.color", havingValue = "true")
    public LogDecorator colorLogDecorator() {
        return new ColorLogDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(LogDecorator.class)
    public LogDecorator plainLogDecorator() {
        return new PlainLogDecorator();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggedAspect loggedAspect(LogDecorator logDecorator, LoggedProperties loggedProperties) {
        LogStrings.enableUtf();
        return new LoggedAspect(loggedProperties, logDecorator);
    }

}
