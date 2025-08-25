package io.github.darkona.logged;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy()
@ComponentScan(basePackages = {"io.github.darkona.logged"})
public class TestBootConfig {

}
