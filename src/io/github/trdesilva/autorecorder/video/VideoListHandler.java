/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.clip.VideoMetadataReader;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import org.joda.time.DateTime;

import java.awt.Image;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VideoListHandler
{
    private final Settings settings;
    private final VideoMetadataReader metadataReader;
    private final StatusQueue status;
    private final VideoFilenameValidator filenameValidator;
    private final VideoType type;
    
    private File videoDir;
    
    @AssistedInject
    public VideoListHandler(Settings settings, VideoMetadataReader metadataReader, StatusQueue status, VideoFilenameValidator filenameValidator, @Assisted VideoType type)
    {
        this.settings = settings;
        this.metadataReader = metadataReader;
        this.status = status;
        this.filenameValidator = filenameValidator;
        this.type = type;
        
        update();
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
        
        if(videoDir != null && videoDir.equals(video.getParentFile()) && filenameValidator.hasValidName(video.getName()))
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
            status.postMessage(new StatusMessage(StatusType.WARNING, String.format("Problem reading %s directory; check your settings", type.name().toLowerCase())));
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
        status.postMessage(new StatusMessage(StatusType.DEBUG,
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
}
