/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload;

import java.util.HashMap;
import java.util.Map;

public class UploadJob
{
    String clipName;
    String videoTitle;
    String description;
    Map<String, String> properties;
    
    public UploadJob(String clipName, String videoTitle, String description)
    {
        this.clipName = clipName;
        this.videoTitle = videoTitle;
        this.description = description;
        properties = new HashMap<>();
    }
    
    public String getClipName()
    {
        return clipName;
    }
    
    public void setClipName(String clipName)
    {
        this.clipName = clipName;
    }
    
    public String getVideoTitle()
    {
        return videoTitle;
    }
    
    public void setVideoTitle(String videoTitle)
    {
        this.videoTitle = videoTitle;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public void addProperty(String name, String value)
    {
        properties.put(name, value);
    }
    
    public String getProperty(String name)
    {
        return properties.get(name);
    }
    
    @Override
    public String toString()
    {
        return "UploadJob{" +
                "clipName='" + clipName + '\'' +
                ", videoTitle='" + videoTitle + '\'' +
                ", description='" + description + '\'' +
                ", properties=" + properties +
                '}';
    }
}
