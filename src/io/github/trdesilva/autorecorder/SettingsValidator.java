/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.File;

public class SettingsValidator
{
    public static boolean validate(Settings settings)
    {
        if(settings.getObsPath().isBlank())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "OBS path is unset in settings. You can download OBS Studio here:", "https://obsproject.com/download"));
            return false;
        }
        File obsFile = new File(settings.getObsPath());
        if(!obsFile.exists())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "OBS path doesn't point to a file"));
            return false;
        }
        if(!settings.getObsPath().endsWith(".exe") || !obsFile.canExecute())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "OBS path doesn't point to an executable"));
            return false;
        }
    
        if(settings.getRecordingPath().isBlank())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Recording path is unset in settings"));
            return false;
        }
        File recordingFile = new File(settings.getRecordingPath());
        if(!recordingFile.exists() || !recordingFile.isDirectory())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Recording path doesn't point to a directory"));
            return false;
        }
    
        if(settings.getClipPath().isBlank())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Clip path is unset in settings"));
            return false;
        }
        File clipFile = new File(settings.getClipPath());
        if(!clipFile.exists() || !clipFile.isDirectory())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Clip path doesn't point to a directory"));
            return false;
        }
        
        return true;
    }
}
