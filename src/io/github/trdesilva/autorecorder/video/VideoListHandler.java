/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.clip.VideoMetadataReader;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventConsumer;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;
import org.joda.time.DateTime;

import java.awt.Image;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoListHandler implements EventConsumer
{
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.RECORDING_START,
                                                                            EventType.SETTINGS_CHANGE);
    private final Settings settings;
    private final VideoMetadataReader metadataReader;
    private final EventQueue events;
    private final VideoFilenameValidator filenameValidator;
    private final VideoType type;
    
    private File videoDir;
    
    @AssistedInject
    public VideoListHandler(Settings settings, VideoMetadataReader metadataReader, EventQueue events,
                            VideoFilenameValidator filenameValidator, @Assisted VideoType type)
    {
        this.settings = settings;
        this.metadataReader = metadataReader;
        this.events = events;
        this.filenameValidator = filenameValidator;
        this.type = type;
        
        update();
        events.addConsumer(this);
    }
    
    public File getVideo(String name)
    {
        File video;
        if(Paths.get(name).isAbsolute())
        {
            video = new File(name);
        }
        else
        {
            video = new File(videoDir, name);
        }
        
        if(videoDir != null && videoDir.equals(video.getParentFile()) && filenameValidator.hasValidName(
                video.getName()))
        {
            return video;
        }
        
        return null;
    }
    
    public List<File> getVideoList()
    {
        update();
        if(videoDir != null && videoDir.exists() && videoDir.isDirectory() && videoDir.canRead())
        {
            return Arrays.asList(videoDir.listFiles(((dir, name) -> filenameValidator.hasValidName(name))));
        }
        else
        {
            events.postEvent(new Event(EventType.WARNING,
                                       String.format("Problem reading %s directory; check your settings",
                                                     type.name().toLowerCase())));
            return Collections.emptyList();
        }
    }
    
    public DateTime getCreationDate(String name)
    {
        File video = new File(videoDir, name);
        return getCreationDate(video);
    }
    
    public DateTime getCreationDate(File video)
    {
        return metadataReader.getCreationDate(video);
    }
    
    public long getDuration(String name)
    {
        File video = new File(videoDir, name);
        return getDuration(video);
    }
    
    public long getDuration(File video)
    {
        return metadataReader.getDuration(video);
    }
    
    public String getResolution(String name)
    {
        File video = new File(videoDir, name);
        return getResolution(video);
    }
    
    public String getResolution(File video)
    {
        return metadataReader.getResolution(video);
    }
    
    public Image getThumbnail(String name)
    {
        File video = new File(videoDir, name);
        return getThumbnail(video);
    }
    
    public Image getThumbnail(File video)
    {
        return metadataReader.getThumbnail(video);
    }
    
    public void update()
    {
        String updatedSetting = type.getPathSetting(settings);
        events.postEvent(new Event(EventType.DEBUG,
                                   String.format("New %s directory: %s", type.name(), updatedSetting)));
        if(updatedSetting != null)
        {
            File updatedDir = new File(updatedSetting);
            if(!updatedDir.equals(videoDir) && updatedDir.exists() && updatedDir.isDirectory())
            {
                this.videoDir = updatedDir;
            }
        }
    }
    
    public void runAutoDelete()
    {
        if(type == VideoType.RECORDING && settings.isAutoDeleteEnabled())
        {
            events.postEvent(new Event(EventType.DEBUG, "Starting autodelete check"));
            // sort videos oldest to newest (in deletion order)
            List<File> videoList = getVideoList().stream()
                                                 .sorted((f1, f2) -> (int) (f1.lastModified() - f2.lastModified()))
                                                 .collect(Collectors.toList());
            // don't automatically delete if there's only one video because that might be the active recording
            if(videoList.size() < 2)
            {
                events.postEvent(new Event(EventType.DEBUG, "Less than 2 videos, not deleting"));
                return;
            }
            
            long totalSize = 0;
            long threshold = settings.getAutoDeleteThresholdGB() * 1024L * 1024L * 1024L;
            for(File video : videoList)
            {
                totalSize += video.length();
            }
            
            if(totalSize > threshold)
            {
                events.postEvent(new Event(EventType.INFO, String.format(
                        "Recording storage is over %dGB capacity; cleaning up old recordings",
                        settings.getAutoDeleteThresholdGB())));
            }
            
            int i = 0;
            int deletedVideos = 0;
            while(totalSize > threshold && i < videoList.size())
            {
                File video = videoList.get(i++);
                totalSize -= video.length();
                if(video.delete())
                {
                    deletedVideos++;
                }
                else
                {
                    events.postEvent(
                            new Event(EventType.DEBUG, "Failed to delete " + video.getAbsolutePath()));
                    totalSize += video.length(); // undo decrement because the file is still there
                }
            }
            
            if(deletedVideos > 0)
            {
                events.postEvent(
                        new Event(EventType.INFO, String.format("Deleted %d recordings", deletedVideos)));
            }
            else
            {
                events.postEvent(new Event(EventType.DEBUG,
                                           String.format("Nothing deleted, total size %d", totalSize)));
            }
        }
    }
    
    @Override
    public void post(Event message)
    {
        if(message.getType().equals(EventType.RECORDING_START))
        {
            new Thread(() -> {
                runAutoDelete();
                update();
            }).start();
        }
        else if(message.getType().equals(EventType.SETTINGS_CHANGE))
        {
            new Thread(this::update).start();
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
}
