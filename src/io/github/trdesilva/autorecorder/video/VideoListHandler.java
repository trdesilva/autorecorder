/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import org.joda.time.DateTime;

import java.awt.Image;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoListHandler implements EventConsumer
{
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.RECORDING_START,
                                                                            EventType.SETTINGS_CHANGE,
                                                                            EventType.BOOKMARK,
                                                                            EventType.UPLOAD_END);
    private final Settings settings;
    private final VideoMetadataHandler metadataHandler;
    private final EventQueue events;
    private final VideoFilenameValidator filenameValidator;
    private final VideoType type;
    
    private File videoDir;
    
    @AssistedInject
    public VideoListHandler(Settings settings, VideoMetadataHandler metadataHandler, EventQueue events,
                            VideoFilenameValidator filenameValidator, @Assisted VideoType type)
    {
        this.settings = settings;
        this.metadataHandler = metadataHandler;
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
    
    public VideoMetadata getMetadata(File video)
    {
        return metadataHandler.getMetadata(video);
    }
    
    public void saveMetadata(File video, VideoMetadata metadata)
    {
        metadataHandler.saveMetadata(video, metadata);
    }
    
    public synchronized void saveBookmark(long timestamp)
    {
        File recording = getMostRecentRecording();
        metadataHandler.saveBookmark(recording, timestamp);
        events.postEvent(new Event(EventType.INFO, "Saved bookmark at " + TimestampUtil.formatTime(timestamp)));
    }
    
    public void update()
    {
        String updatedSetting = type.getPathSetting(settings);
        if(updatedSetting != null)
        {
            File updatedDir = new File(updatedSetting);
            if(!updatedDir.equals(videoDir) && updatedDir.exists() && updatedDir.isDirectory())
            {
                this.videoDir = updatedDir;
                events.postEvent(new Event(EventType.DEBUG,
                                           String.format("New %s directory: %s", type.name(), updatedSetting)));
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
                    .sorted(Comparator.comparing(File::lastModified))
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
                    metadataHandler.deleteMetadata(video);
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
    public void post(Event event)
    {
        if(event.getType().equals(EventType.RECORDING_START))
        {
            new Thread(() -> {
                runAutoDelete();
                update();
                if(type.equals(VideoType.RECORDING))
                {
                    File mostRecent = getMostRecentRecording();
                    VideoMetadata metadata = getMetadata(mostRecent);
                    int tries = 0;
                    while(!(metadata.getGameName().isBlank() && new DateTime(mostRecent.lastModified()).isAfter(
                            DateTime.now().minusSeconds(5))))
                    {
                        try
                        {
                            mostRecent = getMostRecentRecording();
                            metadata = getMetadata(mostRecent);
                            events.postEvent(new Event(EventType.DEBUG, "most recent: " + mostRecent.getName()));
                            if(++tries > 10)
                            {
                                events.postEvent(new Event(EventType.WARNING, "Could not find new recording; did OBS start?"));
                                return;
                            }
                            Thread.sleep(1000);
                        }
                        catch(InterruptedException e)
                        {
                            events.postEvent(new Event(EventType.DEBUG, "Metadata polling sleep interrupted"));
                        }
                    }
                    String gameName = (String) event.getProperties().get(EventProperty.GAME_NAME);
                    metadata.setGameName(gameName);
                    metadataHandler.saveMetadata(mostRecent, metadata);
                    events.postEvent(new Event(EventType.DEBUG, String.format("Game name %s set on metadata for %s", gameName, mostRecent.getName())));
                }
            }).start();
        }
        else if(event.getType().equals(EventType.SETTINGS_CHANGE))
        {
            new Thread(this::update).start();
        }
        else if(event.getType().equals(EventType.BOOKMARK))
        {
            saveBookmark((Long)(event.getProperties().get(EventProperty.BOOKMARK_TIME)));
        }
        else if(event.getType().equals(EventType.UPLOAD_END))
        {
            if(type.equals(VideoType.CLIP) && event.getProperties().containsKey(EventProperty.LINK))
            {
                UploadJob uploadJob = (UploadJob) event.getProperties().get(EventProperty.UPLOAD_JOB);
                File video = getVideo(uploadJob.getClipName());
                VideoMetadata metadataToUpdate = metadataHandler.getMetadata(video);
                metadataToUpdate.setUploadLink((String) event.getProperties().get(EventProperty.LINK));
                saveMetadata(video, metadataToUpdate);
            }
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
    
    private File getMostRecentRecording()
    {
        File recording = getVideoList().stream()
                                       .max(Comparator.comparing(File::lastModified))
                                       .get();
        return recording;
    }
    
}
