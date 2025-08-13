package com.darkona.logged;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAspectJAutoProxy()
@ComponentScan(basePackages = "com.darkona.logged")
public class TestBootConfig {


    @Bean
    public LoggedAspect loggedAspect(LogDecorator decorator) {
        return new LoggedAspect(new LoggedProperties(), decorator);
    }

}
