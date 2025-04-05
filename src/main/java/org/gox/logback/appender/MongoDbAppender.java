package org.gox.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;

public class MongoDbAppender extends AsyncAppenderBase<ILoggingEvent> {

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        super.append(eventObject);
    }
}