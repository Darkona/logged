package io.github.darkona.logged.plugins.mdc;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logged.mdc")
@Data
public class LoggedMdcProperties {

    /**
     * Enable the plugin
     */
    private boolean enabled = false;

    /**
     * Template for arguments or parameters of the method.
     * The only tokens for substitution that work here are {c} for class name, {k} for the parameter name and {v} for the value.
     * <p>Default: {@code "({c}) {k}={v}"}</p>
     * <p>Meaning: Logs argument type, name and value</p>
     */
    @Getter
    @Setter
    private String argsTemplate = "({c}) {k}={v}";


}
