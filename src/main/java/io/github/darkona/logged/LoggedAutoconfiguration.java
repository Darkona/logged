package io.github.darkona.logged;

import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.internals.ColorLogDecorator;
import io.github.darkona.logged.internals.LoggedEngine;
import io.github.darkona.logged.internals.LoggedAspect;
import io.github.darkona.logged.internals.PlainLogDecorator;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.weaving.BridgeInstaller;
import io.github.darkona.logged.weaving.Conditions;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass({LoggedEngine.class, Logged.class, Logger.class, LogDecorator.class, LoggedProperties.class})
@EnableConfigurationProperties({LoggedProperties.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan("io.github.darkona.logged")
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
    public LoggedEngine loggedEngine(LogDecorator logDecorator, LoggedProperties loggedProperties, List<LoggedPlugin> plugins) {
        return new LoggedEngine(loggedProperties, logDecorator, plugins);
    }

    @Bean
    @Conditional(Conditions.OnNoAspectJWeaving.class)
    @ConditionalOnBooleanProperty(value = "logged.enabled", matchIfMissing = true)
    public LoggedAspect springAspect(LoggedEngine engine) {
        return new LoggedAspect(engine);
    }

    @Bean
    @Conditional(Conditions.OnAspectJWeaving.class)
    @ConditionalOnBooleanProperty(value = "logged.enabled", matchIfMissing = true)
    public BridgeInstaller bridgeInstaller(LoggedEngine loggedEngine) {
        return new BridgeInstaller(loggedEngine);
    }

}
