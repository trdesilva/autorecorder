/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;

public class VideoMetadata
{
    private DateTime creationDate = new DateTime(0);
    private long duration = -1;
    private String resolution = "N/A";
    private List<Long> bookmarks = new LinkedList<>();
    
    public DateTime getCreationDate()
    {
        return creationDate;
    }
    
    public void setCreationDate(DateTime creationDate)
    {
        this.creationDate = creationDate;
    }
    
    public long getDuration()
    {
        return duration;
    }
    
    public void setDuration(long duration)
    {
        this.duration = duration;
    }
    
    public String getResolution()
    {
        return resolution;
    }
    
    public void setResolution(String resolution)
    {
        this.resolution = resolution;
    }
    
    public List<Long> getBookmarks()
    {
        return bookmarks;
    }
    
    public void setBookmarks(List<Long> bookmarks)
    {
        this.bookmarks = bookmarks;
    }
}
