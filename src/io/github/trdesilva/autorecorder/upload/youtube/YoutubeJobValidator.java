/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.video.VideoFilenameValidator;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class YoutubeJobValidator implements UploadJobValidator
{
    private final StatusQueue status;
    private final VideoFilenameValidator videoFilenameValidator;
    private final VideoListHandler clipListHandler;
    
    @Inject
    public YoutubeJobValidator(StatusQueue status, VideoFilenameValidator videoFilenameValidator,
                               @Named("CLIP") VideoListHandler clipListHandler)
    {
        this.status = status;
        this.videoFilenameValidator = videoFilenameValidator;
        this.clipListHandler = clipListHandler;
    }
    
    @Override
    public boolean validate(UploadJob job)
    {
        String clipName = Paths.get(job.getClipName()).getFileName().toString();
        if(!videoFilenameValidator.hasValidName(clipName))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Clip has invalid format"));
            return false;
        }
        
        File clip = clipListHandler.getVideo(job.getClipName());
        if(clip == null || !clip.exists())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Clip file doesn't exist"));
            return false;
        }
        
        if(job.getVideoTitle().isBlank())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Video title may not be blank"));
            return false;
        }
        if(job.getVideoTitle().length() > 100)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Video title must be 100 characters or less"));
            return false;
        }
        if(job.getVideoTitle().contains(">") || job.getVideoTitle().contains("<"))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Video title may not contain '<' or '>'"));
            return false;
        }
        
        if(job.getDescription().length() > 5000)
        {
            status.postMessage(
                    new StatusMessage(StatusType.WARNING, "Video description must be 5000 characters or less"));
            return false;
        }
        if(job.getDescription().contains(">") || job.getDescription().contains("<"))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Video description may not contain '<' or '>'"));
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
            status.postMessage(new StatusMessage(StatusType.WARNING, "Invalid privacy status"));
            return false;
        }
        
        return true;
    }
}
