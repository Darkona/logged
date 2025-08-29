package io.github.darkona.logged.plugins.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

public class Log4jMarkerFilter implements Filter {

    private final String name;
    private final Result onMatch;
    private final Result onMismatch;
    private boolean running = false;

    public Log4jMarkerFilter(String name, Result onMatch, Result onMismatch) {
        this.name = name;
        this.onMatch = onMatch;
        this.onMismatch = onMismatch;
    }

    @Override
    public Result getOnMismatch() {
        return onMismatch;
    }

    @Override
    public Result getOnMatch() {
        return onMatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String msg, Object... params) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, Object msg, Throwable t) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(Logger logger, Level level, org.apache.logging.log4j.Marker marker, Message msg, Throwable t) {
        return marker.getName().equals(name) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(LogEvent event) {
        return event.getMarker()!=null ? (event.getMarker().getName().equals(name) ? onMatch : onMismatch )
                                       : Result.NEUTRAL;
    }

    @Override
    public State getState() {
        return running ? State.STARTED : State.STOPPED;
    }

    @Override
    public void initialize() {
        running = true;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isStarted() {
        return running;
    }

    @Override
    public boolean isStopped() {
        return !running;
    }
}
