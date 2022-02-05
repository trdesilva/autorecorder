/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.status;

import org.joda.time.DateTime;

public class StatusMessage
{
    private StatusType type;
    private String message;
    private String link;
    private DateTime timestamp;
    
    public StatusMessage(StatusType type, String message)
    {
        this(type, message, null);
    }
    
    public StatusMessage(StatusType type, String message, String link)
    {
        this.type = type;
        this.message = message;
        this.link = link;
        
        timestamp = DateTime.now();
    }
    
    public StatusType getType()
    {
        return type;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public String getLink()
    {
        return link;
    }
    
    public DateTime getTimestamp()
    {
        return timestamp;
    }
}
