package io.github.darkona.logged;

import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.internals.ColorLogDecorator;
import io.github.darkona.logged.internals.LoggedAspect;
import io.github.darkona.logged.internals.LoggedEngine;
import io.github.darkona.logged.internals.PlainLogDecorator;
import io.github.darkona.logged.weaving.BridgeInstaller;
import io.github.darkona.logged.weaving.Conditions;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(name = {"org.slf4j.Logger", "org.slf4j.LoggerFactory"})
@ConditionalOnBooleanProperty(value = "logged.enabled", matchIfMissing = true)
@EnableConfigurationProperties({LoggedProperties.class})
@EnableAspectJAutoProxy
public class LoggedAutoconfiguration {


    @Bean
    @ConditionalOnBooleanProperty(value = "logged.color", matchIfMissing = true)
    @ConditionalOnMissingBean(LogDecorator.class)
    public LogDecorator colorLogDecorator() {
        return new ColorLogDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(LogDecorator.class)
    public LogDecorator plainLogDecorator() {
        return new PlainLogDecorator();
    }

    @Bean("loggedEngine")
    @Conditional(Conditions.OnAspectJWeaving.class)
    public LoggedEngine wovenEngine(LogDecorator logDecorator, LoggedProperties loggedProperties, List<LoggedPlugin> plugins) {
        var engine = new LoggedEngine(loggedProperties, logDecorator, plugins);
        engine.setWoven(true);
        return engine;
    }

    @Bean("loggedEngine")
    @Conditional(Conditions.OnNoAspectJWeaving.class)
    public LoggedEngine loggedEngine(LogDecorator logDecorator, LoggedProperties loggedProperties, List<LoggedPlugin> plugins) {
        return new LoggedEngine(loggedProperties, logDecorator, plugins);
    }

    @Bean
    @Conditional(Conditions.OnNoAspectJWeaving.class)
    public LoggedAspect springAspect(LoggedEngine engine) {
        return new LoggedAspect(engine);
    }

    @Bean
    @Conditional(Conditions.OnAspectJWeaving.class)
    public BridgeInstaller bridgeInstaller(LoggedEngine engine) {
        return new BridgeInstaller(engine);
    }


}
