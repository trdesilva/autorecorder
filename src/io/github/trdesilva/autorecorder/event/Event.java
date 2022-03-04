/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.event;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Map;

public class Event
{
    private EventType type;
    private String message;
    private Map<EventProperty, Object> properties;
    
    private DateTime timestamp;
    private StackTraceElement source;
    
    public Event(EventType type, String message)
    {
        this(type, message, Collections.emptyMap());
        // 0 is getStackTrace(), 1 is this constructor, 2 is the caller
        source = Thread.currentThread().getStackTrace()[2];
    }
    
    public Event(EventType type, String message, Map<EventProperty, Object> properties)
    {
        this.type = type;
        this.message = message;
        this.properties = properties;
        
        timestamp = DateTime.now();
        // 0 is getStackTrace(), 1 is this constructor, 2 is the caller
        source = Thread.currentThread().getStackTrace()[2];
    }
    
    public EventType getType()
    {
        return type;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public Map<EventProperty, Object> getProperties()
    {
        return properties;
    }
    
    public DateTime getTimestamp()
    {
        return timestamp;
    }
    
    public StackTraceElement getSource()
    {
        return source;
    }
    
    @Override
    public String toString()
    {
        return String.format("%s %s [%s.%s] %s %s", timestamp.toString(), type.name(), source.getClassName(),
                             source.getMethodName(), message, properties.toString());
    }
}
