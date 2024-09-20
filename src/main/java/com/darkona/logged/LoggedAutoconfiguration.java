package com.darkona.logged;

import com.darkona.logged.annotation.Logged;
import com.darkona.logged.utils.LogStrings;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({LoggedAspect.class, Logged.class, Logger.class})
public class LoggedAutoconfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoggedAspect loggedAspect() {
        LogStrings.enableUtf();

        return new LoggedAspect();
    }

}
