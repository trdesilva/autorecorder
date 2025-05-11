/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.record.Obs;
import io.github.trdesilva.autorecorder.video.Hotkey;

import java.io.File;
import java.util.Collections;

public class SettingsValidator
{
    private final EventQueue events;
    private final Obs obs;
    
    @Inject
    public SettingsValidator(EventQueue events, Obs obs)
    {
        this.events = events;
        this.obs = obs;
    }
    
    public boolean validate(Settings.SettingsContainer settings)
    {
        if(settings.obsPath.isBlank())
        {
            events.postEvent(new Event(EventType.WARNING,
                                       "OBS path is unset in settings. You can download OBS Studio here:",
                                       Collections.singletonMap(EventProperty.LINK, "https://obsproject.com/download")));
            return false;
        }
        File obsFile = new File(settings.obsPath);
        if(!obsFile.exists())
        {
            events.postEvent(new Event(EventType.WARNING, "OBS path doesn't point to a file"));
            return false;
        }
        if(!settings.obsPath.endsWith(".exe") || !obsFile.canExecute())
        {
            events.postEvent(new Event(EventType.WARNING, "OBS path doesn't point to an executable"));
            return false;
        }
        
        if(settings.obsProfileName.isBlank())
        {
            events.postEvent(new Event(EventType.WARNING,
                                       "OBS profile name is unset in settings. Is OBS Studio installed?",
                                       Collections.singletonMap(EventProperty.LINK, "https://obsproject.com/kb/profiles")));
            return false;
        }
        
        if(!obs.readProfileNames().contains(settings.obsProfileName))
        {
            events.postEvent(new Event(EventType.WARNING,
                                       "Selected OBS profile does not exist in OBS profile directory",
                                       Collections.singletonMap(EventProperty.LINK, "https://obsproject.com/forum/threads/where-are-profiles-saved.462/")));
            return false;
        }
        
        if(settings.recordingPath.isBlank())
        {
            events.postEvent(new Event(EventType.WARNING, "Recording path is unset"));
            return false;
        }
        File recordingFile = new File(settings.recordingPath);
        if(!recordingFile.exists() || !recordingFile.isDirectory())
        {
            events.postEvent(new Event(EventType.WARNING, "Recording path doesn't point to a directory"));
            return false;
        }
        
        if(settings.clipPath.isBlank())
        {
            events.postEvent(new Event(EventType.WARNING, "Clip path is unset"));
            return false;
        }
        File clipFile = new File(settings.clipPath);
        if(!clipFile.exists() || !clipFile.isDirectory())
        {
            events.postEvent(new Event(EventType.WARNING, "Clip path doesn't point to a directory"));
            return false;
        }
        
        if(settings.autoDeleteThresholdGB < 0)
        {
            events.postEvent(new Event(EventType.WARNING, "Maximum recording space must be a non-negative integer"));
            return false;
        }
        
        if(settings.bookmarksEnabled && settings.bookmarkKey.getKeyCode() == Hotkey.UNSET)
        {
            events.postEvent(new Event(EventType.WARNING, "Bookmark hotkey must be set if bookmarking is enabled"));
            return false;
        }
        
        if(settings.consumeWindowsKeyEnabled && !settings.bookmarksEnabled)
        {
            events.postEvent(new Event(EventType.WARNING, "Disabling Windows key requires bookmark hotkey to be enabled"));
            return false;
        }
        
        return true;
    }
}
