/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.video.VideoFilenameValidator;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClipJobValidator
{
    private final Settings settings;
    private final StatusQueue status;
    private final VideoFilenameValidator videoFilenameValidator;
    
    @Inject
    public ClipJobValidator(Settings settings, StatusQueue status, VideoFilenameValidator videoFilenameValidator)
    {
        this.settings = settings;
        this.status = status;
        this.videoFilenameValidator = videoFilenameValidator;
    }
    
    public boolean validate(ClipJob job)
    {
        if(!validateVideo(job.getSource(), VideoSource.RECORDING))
        {
            return false;
        }
        
        if(!validateVideo(job.getDest(), VideoSource.CLIP))
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
    
    private boolean validateVideo(String video, VideoSource source)
    {
        String directory;
        String label;
        boolean shouldExist;
        
        if(source == VideoSource.RECORDING)
        {
            directory = settings.getRecordingPath();
            label = "Recording";
            shouldExist = true;
        }
        else
        {
            directory = settings.getClipPath();
            label = "Clip";
            shouldExist = false;
        }
        
        if(video == null || video.isBlank())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " path cannot be blank"));
            return false;
        }
        
        Path directoryPath = Paths.get(directory);
        Path videoPath;
        if(video.startsWith(settings.getRecordingPath()))
        {
            videoPath = Paths.get(video);
        }
        else
        {
            videoPath = directoryPath.resolve(video);
        }
        
        if(!videoPath.getParent().equals(directoryPath))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " is not in recording directory: " + video));
            return false;
        }
        File videoFile = videoPath.toFile();
        if(!videoFile.exists() && shouldExist)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " doesn't exist: " + video));
            return false;
        }
        else if(videoFile.exists() && !shouldExist)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " already exists: " + video));
            return false;
        }
        
        if(!videoFilenameValidator.hasValidName(videoFile.getName()))
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, label + " has invalid filename: " + video));
            return false;
        }
        
        return true;
    }
    
    private enum VideoSource
    {
        RECORDING,
        CLIP
    }
}
