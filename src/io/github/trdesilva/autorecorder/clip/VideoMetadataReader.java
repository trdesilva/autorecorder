/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;
import org.joda.time.DateTime;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class VideoMetadataReader
{
    private final Settings settings;
    private final EventQueue events;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Map<File, JsonNode> metadataMapping;
    
    @Inject
    public VideoMetadataReader(Settings settings, EventQueue events)
    {
        this.settings = settings;
        this.events = events;
        
        metadataMapping = new HashMap<>();
    }
    
    public DateTime getCreationDate(File video)
    {
        if(video != null && video.exists())
        {
            return new DateTime(video.lastModified());
        }
        return new DateTime(0);
    }
    
    public long getDuration(File video)
    {
        parseVideo(video);
        if(video != null && metadataMapping.containsKey(video))
        {
            JsonNode metadata = metadataMapping.get(video);
            // duration won't be filled in until recording is complete, so if it's missing, try parsing again
            if(!metadata.get("format").has("duration"))
            {
                metadataMapping.remove(video);
                parseVideo(video);
            }
    
            // check again after reparse
            if(metadata.get("format").has("duration"))
            {
                double durationSeconds = metadataMapping.get(video).get("format").get("duration").asDouble();
                return (long) (durationSeconds * 1000);
            }
        }
        return -1;
    }
    
    public String getResolution(File video)
    {
        parseVideo(video);
        if(video != null && metadataMapping.containsKey(video))
        {
            JsonNode json = metadataMapping.get(video);
            int width = json.get("streams").get(0).get("width").asInt();
            int height = json.get("streams").get(0).get("height").asInt();
            return String.format("%dx%d", width, height);
        }
        return "N/A";
    }
    
    public Image getThumbnail(File video)
    {
        return null;
    }
    
    private void parseVideo(File video)
    {
        if(video != null && video.exists() && !metadataMapping.containsKey(video))
        {
            try
            {
                metadataMapping.put(video, getMetadataJson(video));
            }
            catch(IOException e)
            {
                events.postEvent(
                        new Event(EventType.WARNING, "Couldn't read metadata for " + video.getName()));
            }
        }
    }
    
    private JsonNode getMetadataJson(File video) throws IOException
    {
        events.postEvent(new Event(EventType.DEBUG, "reading metadata for " + video.getAbsolutePath()));
        String[] ffprobeArgs = {settings.getFfprobePath(), "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", "-select_streams", "v:0", video.getAbsolutePath()};
        Process ffprobeProc = Runtime.getRuntime()
                                     .exec(ffprobeArgs, null,
                                           new File(Paths.get(settings.getFfmpegPath()).getParent().toString()));
        InputStream stdout = ffprobeProc.getInputStream();
        try
        {
            ffprobeProc.waitFor(2000, TimeUnit.MILLISECONDS);
        }
        catch(InterruptedException e)
        {
            throw new IOException(e);
        }
        
        return objectMapper.readTree(stdout);
    }
}
