package com.darkona.logged;

import com.darkona.logged.internals.LogDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy()
@ComponentScan(basePackages = "com.darkona.logged")
public class TestBootConfig {


    @Bean
    public LoggedAspect loggedAspect(LogDecorator decorator) {
        return new LoggedAspect(new LoggedProperties(), decorator);
    }

}
