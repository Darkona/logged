package io.github.darkona.logged;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Plugin(name = "ListAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class Log4j2ListAppender extends AbstractAppender {


    private final ConcurrentLinkedQueue<LogEvent> list = new ConcurrentLinkedQueue<>();


    protected Log4j2ListAppender(
            final String name,
            final Filter filter,
            final Layout<? extends Serializable> layout,
            final boolean ignoreExceptions,
            final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @PluginFactory
    public static Log4j2ListAppender create(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout) {
        return new Log4j2ListAppender(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    public List<LogEvent> getList() {
        return list.stream().toList();
    }

    @Override
    public void append(LogEvent event) {
        list.add(event.toImmutable()); // Ensure thread-safety by making the event immutable
    }

    public void clearList() {
        list.clear();
    }
}
