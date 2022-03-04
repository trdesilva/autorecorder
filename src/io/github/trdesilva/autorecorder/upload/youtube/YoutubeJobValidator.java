/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.video.VideoFilenameValidator;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import java.io.File;
import java.nio.file.Paths;

public class YoutubeJobValidator implements UploadJobValidator
{
    private final EventQueue events;
    private final VideoFilenameValidator videoFilenameValidator;
    private final VideoListHandler clipListHandler;
    
    @Inject
    public YoutubeJobValidator(EventQueue events, VideoFilenameValidator videoFilenameValidator,
                               @Named("CLIP") VideoListHandler clipListHandler)
    {
        this.events = events;
        this.videoFilenameValidator = videoFilenameValidator;
        this.clipListHandler = clipListHandler;
    }
    
    @Override
    public boolean validate(UploadJob job)
    {
        String clipName = Paths.get(job.getClipName()).getFileName().toString();
        if(!videoFilenameValidator.hasValidName(clipName))
        {
            events.postEvent(new Event(EventType.WARNING, "Clip has invalid format"));
            return false;
        }
        
        File clip = clipListHandler.getVideo(job.getClipName());
        if(clip == null || !clip.exists())
        {
            events.postEvent(new Event(EventType.WARNING, "Clip file doesn't exist"));
            return false;
        }
        
        if(job.getVideoTitle().isBlank())
        {
            events.postEvent(new Event(EventType.WARNING, "Video title may not be blank"));
            return false;
        }
        if(job.getVideoTitle().length() > 100)
        {
            events.postEvent(new Event(EventType.WARNING, "Video title must be 100 characters or less"));
            return false;
        }
        if(job.getVideoTitle().contains(">") || job.getVideoTitle().contains("<"))
        {
            events.postEvent(new Event(EventType.WARNING, "Video title may not contain '<' or '>'"));
            return false;
        }
        
        if(job.getDescription().length() > 5000)
        {
            events.postEvent(
                    new Event(EventType.WARNING, "Video description must be 5000 characters or less"));
            return false;
        }
        if(job.getDescription().contains(">") || job.getDescription().contains("<"))
        {
            events.postEvent(new Event(EventType.WARNING, "Video description may not contain '<' or '>'"));
            return false;
        }
        
        PrivacyStatus privacyStatus;
        try
        {
            privacyStatus = PrivacyStatus.valueOf(job.getProperty(YoutubeUploader.PRIVACY_PROPERTY));
        }
        catch(IllegalArgumentException e)
        {
            privacyStatus = null;
        }
        if(privacyStatus == null)
        {
            events.postEvent(new Event(EventType.WARNING, "Invalid privacy status"));
            return false;
        }
        
        return true;
    }
}
