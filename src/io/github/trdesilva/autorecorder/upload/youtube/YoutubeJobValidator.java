/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;

import java.io.File;
import java.util.Set;

public class YoutubeJobValidator implements UploadJobValidator
{
    private final Set<String> ALLOWED_EXTENSIONS = Sets.newHashSet(".mpg", ".mpeg", ".mp4", ".mkv", ".mov", ".avi",
                                                                   ".wmv");
    
    private final StatusQueue status;
    
    @Inject
    public YoutubeJobValidator(StatusQueue status)
    {
        this.status = status;
    }
    
    @Override
    public boolean validate(UploadJob job)
    {
        File clip = new File(job.getClipName());
        if(!clip.exists())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Clip file doesn't exist"));
            return false;
        }
        if(!ALLOWED_EXTENSIONS.contains(clip.getName().substring(clip.getName().indexOf('.'))))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Clip has invalid format"));
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
