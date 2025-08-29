package io.github.darkona.logged.plugins.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Marker;

@Setter
@RequiredArgsConstructor
public class LogbackMarkerFilter extends Filter<ILoggingEvent> {


    private final String name;
    private final FilterReply onMatch;
    private final FilterReply onMismatch;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted() || event.getMarkerList() == null) return FilterReply.NEUTRAL;
        return event.getMarkerList().stream()
                    .map(Marker::getName)
                    .anyMatch(markerName -> markerName.equals(name)) ? onMatch : onMismatch;
    }


}