package io.github.darkona.logged.plugins.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.FilterAttachable;
import io.github.darkona.logged.Logged;
import io.github.darkona.logged.api.Data;
import io.github.darkona.logged.api.LogDecorator;
import io.github.darkona.logged.api.LoggedPlugin;
import io.github.darkona.logged.colors.Yellow;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Logback integration plugin for @Logged. Wires marker-based filters on startup.
 */
public class LoggedLogbackPlugin implements LoggedPlugin {

    private final LoggedLogbackProperties props;
    private final LogDecorator deco;

    public LoggedLogbackPlugin(LoggedLogbackProperties props, LogDecorator deco) {
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
        return deco.custom(Yellow.KHAKI, "@Logged-Logback Filter Plugin enabled");
    }

    @Override
    public void onLoad() {
        wireFilters();
    }

    @Override
    public void afterMethod() { }

    private void wireFilters() {

        if(!props.isEnabled()) return;

        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        for(LoggedLogbackProperties.AppenderSimpleFilter marker : props.getAppenderFilters()) {

            var app = findInAttachable(root, marker.getAppenderName());

            if(app instanceof FilterAttachable<ILoggingEvent> appender) {

                var filter = new LogbackMarkerFilter(marker.getName(), marker.getOnMatch(), marker.getOnMismatch());

                filter.setContext(ctx);
                filter.start();

                appender.addFilter(filter);
            }
        }
    }

    @SuppressWarnings("unchecked, rawtypes")
    public static Appender<ILoggingEvent> findInAttachable(AppenderAttachable attachable, String targetName) {
        for (Iterator<Appender<ILoggingEvent>> it = attachable.iteratorForAppenders(); it.hasNext();) {
            var a = it.next();
            if (targetName.equals(a.getName())) return a;
            if (a instanceof AppenderAttachable<?> nested) {
                var hit = findInAttachable(nested, targetName);
                if (hit != null) return hit;
            }
        }
        return null;
    }
}
