package com.darkona.logged;

import com.darkona.logged.internals.ColorLogDecorator;
import com.darkona.logged.internals.LogDecorator;
import com.darkona.logged.internals.PlainLogDecorator;
import com.darkona.logged.plugins.LoggedPlugin;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass({LoggedAspect.class, Logged.class, Logger.class, LogDecorator.class, LoggedProperties.class})
@EnableConfigurationProperties({LoggedProperties.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan("com.darkona.logged")
public class LoggedAutoconfiguration {

    @Bean
    @Primary
    @ConditionalOnBooleanProperty(value = "logged.color", matchIfMissing = true)
    public LogDecorator colorLogDecorator() {
        return new ColorLogDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(LogDecorator.class)
    public LogDecorator plainLogDecorator() {
        return new PlainLogDecorator();
    }

    @Bean
    @ConditionalOnBooleanProperty(value = "logged.enabled", matchIfMissing = true)
    public LoggedAspect loggedAspect(LogDecorator logDecorator, LoggedProperties loggedProperties, List<LoggedPlugin> plugins) {
        return new LoggedAspect(loggedProperties, logDecorator, plugins);
    }

}
