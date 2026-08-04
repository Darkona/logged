package io.github.darkona.logged.plugins.log4j2;

import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LoggedPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Log4j2 integration plugin for @Logged. Wires marker-based filters on startup.
 */
public class LoggedLog4j2Plugin implements LoggedPlugin {

    private static final Logger log = LoggerFactory.getLogger(LoggedLog4j2Plugin.class);
    private final LoggedLog4j2PluginProperties props;
    private final LogDecorator deco;

    public LoggedLog4j2Plugin(LoggedLog4j2PluginProperties props, LogDecorator deco) {
        this.props = props;
        this.deco = deco;
    }

    @Override
    public void onCall(ProceedingJoinPoint pjp, Data data, Logged options) { }

    @Override
    public void onReturn(ProceedingJoinPoint pjp, Data data, Logged options) { }

    @Override
    public void onException(ProceedingJoinPoint pjp, Data data, Logged options, Throwable exception) { }

    @Override
    public String announceLoad() {
        return deco.magenta("@Logged-Log4j2 Plugin enabled.");
    }

    @Override
    public void onLoad() {
        if(!props.isEnabled()) return;
        org.apache.logging.log4j.core.LoggerContext ctx =
                (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        var cfg = ctx.getConfiguration();

        props.getAppenderFilters().forEach(marker -> {
            org.apache.logging.log4j.core.Appender appender = cfg.getAppender(marker.getAppenderName());
            if (appender instanceof AbstractAppender app) {
                var filter = new Log4jMarkerFilter(marker.getName(), marker.getOnMatch(), marker.getOnMismatch());
                filter.start();
                app.addFilter(filter);
            } else {
                log.warn("@Logged-Log4j2: appender '{}' not found or not filterable; marker filter '{}' not installed",
                        marker.getAppenderName(), marker.getName());
            }
        });
    }

    @Override
    public void afterMethod() {

    }


}
