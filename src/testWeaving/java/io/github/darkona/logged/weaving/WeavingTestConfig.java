package io.github.darkona.logged.weaving;

import io.github.darkona.logged.LoggedAutoconfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = WeavingProbeService.class)
@ImportAutoConfiguration(LoggedAutoconfiguration.class)
public class WeavingTestConfig {
}
