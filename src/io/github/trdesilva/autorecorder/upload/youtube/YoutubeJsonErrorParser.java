/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.gui.ReportableException;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;

import java.io.IOException;

public class YoutubeJsonErrorParser
{
    private static final String QUOTA_EXCEEDED = "quotaExceeded";
    private static final String UPLOAD_LIMIT_EXCEEDED = "uploadLimitExceeded";
    
    private final EventQueue events;
    
    private final ObjectMapper objectMapper;
    
    @Inject
    public YoutubeJsonErrorParser(EventQueue events)
    {
        this.events = events;
        
        this.objectMapper = new ObjectMapper();
    }
    
    public ReportableException parseError(GoogleJsonResponseException e)
    {
        try
        {
            events.postEvent(new Event(EventType.DEBUG, e.getContent()));
            JsonNode errorContent = objectMapper.readTree(e.getContent()).get("errors").get(0);
            String reason = errorContent.get("reason").asText();
            if(e.getStatusCode() == 403)
            {
                if(reason.equals(QUOTA_EXCEEDED))
                {
                    return new ReportableException("Daily global YouTube upload quota exceeded; try again after midnight PST",
                                                   e);
                }
                else
                {
                    return new ReportableException("Not authorized to upload to this account", e);
                }
            }
            else if(e.getStatusCode() == 400)
            {
                if(reason.equals(UPLOAD_LIMIT_EXCEEDED))
                {
                    return new ReportableException("This account has exceeded its video upload limit", e);
                }
                else
                {
                    return new ReportableException("Bad request; check logs for details");
                }
            }
            else if(e.getStatusCode() / 100 == 5)
            {
                return new ReportableException("YouTube server error " + e.getStatusCode(), e);
            }
        }
        catch(IOException e2)
        {
            return new ReportableException("Failed to parse YouTube error", e);
        }
        
        return new ReportableException("Failed to parse YouTube error", e);
    }
}
