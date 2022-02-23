/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.File;

public class SettingsValidator
{
    private final StatusQueue status;
    
    @Inject
    public SettingsValidator(StatusQueue status)
    {
        this.status = status;
    }
    
    public boolean validate(Settings.SettingsContainer settings)
    {
        if(settings.obsPath.isBlank())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING,
                                                 "OBS path is unset in settings. You can download OBS Studio here:",
                                                 "https://obsproject.com/download"));
            return false;
        }
        File obsFile = new File(settings.obsPath);
        if(!obsFile.exists())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "OBS path doesn't point to a file"));
            return false;
        }
        if(!settings.obsPath.endsWith(".exe") || !obsFile.canExecute())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "OBS path doesn't point to an executable"));
            return false;
        }
        
        if(settings.recordingPath.isBlank())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Recording path is unset"));
            return false;
        }
        File recordingFile = new File(settings.recordingPath);
        if(!recordingFile.exists() || !recordingFile.isDirectory())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Recording path doesn't point to a directory"));
            return false;
        }
        
        if(settings.clipPath.isBlank())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Clip path is unset"));
            return false;
        }
        File clipFile = new File(settings.clipPath);
        if(!clipFile.exists() || !clipFile.isDirectory())
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Clip path doesn't point to a directory"));
            return false;
        }
        
        if(settings.autoDeleteThresholdGB < 0)
        {
            status.postMessage(new StatusMessage(StatusType.WARNING, "Maximum recording space must be a non-negative integer"));
            return false;
        }
        
        return true;
    }
}
