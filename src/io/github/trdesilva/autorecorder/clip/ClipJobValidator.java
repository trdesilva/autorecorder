/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.video.VideoFilenameValidator;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import io.github.trdesilva.autorecorder.video.VideoType;

import java.io.File;

public class ClipJobValidator
{
    private final VideoListHandler clipListHandler;
    private final VideoListHandler recordingListHandler;
    private final StatusQueue status;
    private final VideoFilenameValidator videoFilenameValidator;
    
    @Inject
    public ClipJobValidator(@Named("CLIP") VideoListHandler clipListHandler,
                            @Named("RECORDING") VideoListHandler recordingListHandler,
                            StatusQueue status, VideoFilenameValidator videoFilenameValidator)
    {
        this.clipListHandler = clipListHandler;
        this.recordingListHandler = recordingListHandler;
        this.status = status;
        this.videoFilenameValidator = videoFilenameValidator;
    }
    
    public boolean validate(ClipJob job)
    {
        if(!validateVideo(job.getSource(), VideoType.RECORDING))
        {
            return false;
        }
        
        if(!validateVideo(job.getDest(), VideoType.CLIP))
        {
            return false;
        }
        
        long startTime = TimestampUtil.parseTime(job.getStartArg());
        if(startTime == -1)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Start time is invalid: " + job.getStartArg()));
            return false;
        }
    
        long endTime = TimestampUtil.parseTime(job.getEndArg());
        if(endTime == -1)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "End time is invalid: " + job.getEndArg()));
            return false;
        }
        
        if(startTime >= endTime)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Start time must be greater than end time"));
            return false;
        }
    
        return true;
    }
    
    private boolean validateVideo(String video, VideoType source)
    {
        File videoFile;
        String label;
        boolean shouldExist;
        
        if(source == VideoType.RECORDING)
        {
            videoFile = recordingListHandler.getVideo(video);
            label = "Recording";
            shouldExist = true;
        }
        else
        {
            videoFile = clipListHandler.getVideo(video);
            label = "Clip";
            shouldExist = false;
        }
        
        if(video == null || video.isBlank())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " path cannot be blank"));
            return false;
        }
    
        if(!videoFilenameValidator.hasValidName(video))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " has invalid filename: " + video));
            return false;
        }
        
        if(!(videoFile != null && videoFile.exists()) && shouldExist)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " doesn't exist: " + video));
            return false;
        }
        else if(videoFile != null && videoFile.exists() && !shouldExist)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " already exists: " + video));
            return false;
        }
        
        return true;
    }
}
