/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.gui.VideoPlaybackPanel;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VideoMetadataReader
{
    private final Settings settings;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Map<File, JsonNode> metadataMapping;
    
    public VideoMetadataReader(Settings settings)
    {
        this.settings = settings;
        
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
            double durationSeconds = metadataMapping.get(video).get("format").get("duration").asDouble();
            return (long)(durationSeconds*1000);
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
    
    public Image getThumbnail()
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
                StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Couldn't read metadata for " + video.getName()));
            }
        }
    }
    
    private JsonNode getMetadataJson(File video) throws IOException
    {
        String[] ffprobeArgs = {settings.getFfprobePath(), "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", "-select_streams", "v:0", video.getAbsolutePath()};
        Process ffprobeProc = Runtime.getRuntime().exec(ffprobeArgs, null, new File(Paths.get(settings.getFfmpegPath()).getParent().toString()));
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
