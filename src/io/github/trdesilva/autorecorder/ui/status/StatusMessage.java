package io.github.trdesilva.autorecorder.ui.status;

import org.joda.time.DateTime;

public class StatusMessage
{
    private StatusType type;
    private String message;
    private DateTime timestamp;
    
    public StatusMessage(StatusType type, String message)
    {
        this.type = type;
        this.message = message;
        
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
    
    public DateTime getTimestamp()
    {
        return timestamp;
    }
}
