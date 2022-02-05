/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.common.collect.Sets;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;

import java.io.File;
import java.util.Set;

public class YoutubeJobValidator implements UploadJobValidator
{
    Set<String> allowedExtensions = Sets.newHashSet(".mpg", ".mpeg", ".mp4", ".mkv", ".mov", ".avi", ".wmv");
    @Override
    public boolean validate(UploadJob job)
    {
        File clip = new File(job.getClipName());
        if(!clip.exists())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Clip file doesn't exist"));
            return false;
        }
        if(!allowedExtensions.contains(clip.getName().substring(clip.getName().indexOf('.'))))
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Clip has invalid format"));
            return false;
        }
        
        if(job.getVideoTitle().isBlank())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Video title may not be blank"));
            return false;
        }
        if(job.getVideoTitle().length() > 100)
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Video title must be 100 characters or less"));
            return false;
        }
        if(job.getVideoTitle().contains(">") || job.getVideoTitle().contains("<"))
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Video title may not contain '<' or '>'"));
            return false;
        }
        
        if(job.getDescription().length() > 5000)
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Video description must be 5000 characters or less"));
            return false;
        }
        if(job.getDescription().contains(">") || job.getDescription().contains("<"))
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Video description may not contain '<' or '>'"));
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
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Invalid privacy status"));
            return false;
        }
        
        return true;
    }
}
