package io.github.darkona.logged.internals;

import io.github.darkona.logged.api.LoggedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Iterates the registered {@link LoggedPlugin}s, isolating every callback:
 * a broken plugin must never affect the business method or the other plugins.
 */
public class PluginDispatcher {

    private static final Logger log = LoggerFactory.getLogger(PluginDispatcher.class);

    private final List<LoggedPlugin> plugins;

    PluginDispatcher(List<LoggedPlugin> plugins) {
        this.plugins = plugins;
    }

    /** Runs each plugin's onLoad, optionally logging its announceLoad message. */
    void loadAll(boolean announce) {
        plugins.forEach(loggedPlugin -> {
            try {
                loggedPlugin.onLoad();
                if (announce && !loggedPlugin.announceLoad().isBlank()) log.info(loggedPlugin.announceLoad());
            } catch (Exception e) {
                var m = "Error loading plugin: " + loggedPlugin.getClass().getSimpleName();
                log.error(m, e);
            }
        });
    }

    /** Runs the action on every plugin, swallowing and logging per-plugin failures. */
    void dispatch(String phase, Consumer<LoggedPlugin> action) {
        for (LoggedPlugin p : plugins) {
            try {
                action.accept(p);
            } catch (Throwable t) {
                log.warn("@Logged plugin {} failed during {}: {}", p.getClass().getSimpleName(), phase, t.toString());
            }
        }
    }
}
