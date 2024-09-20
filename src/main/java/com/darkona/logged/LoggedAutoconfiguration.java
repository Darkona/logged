package com.darkona.logged.autoconfigure;

import com.darkona.logged.annotation.LoggedAspect;
import com.darkona.logged.utils.LogStrings;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ConditionalOnClass({Logger.class, Aspect.class})
@ComponentScan("com.darkona.logged")
public class LoggedAutoconfiguration {

    public LoggedAutoconfiguration() {
        LogStrings.bannerize("@Logged enabling...", 80);
    }
    @Bean
    @ConditionalOnMissingBean
    public LoggedAspect loggedAspect() {
        LogStrings.enableUtf();

        return new LoggedAspect();
    }

}
