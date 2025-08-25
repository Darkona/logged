package io.github.darkona.logged.plugins.mdc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logged.mdc")
@Data
public class LoggedMdcProperties {

    private boolean enabled = false;
}
