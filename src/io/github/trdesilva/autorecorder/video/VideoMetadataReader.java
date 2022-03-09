/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import org.joda.time.DateTime;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class VideoMetadataReader
{
    private static final Path CACHE_PATH = Settings.SETTINGS_DIR.resolve("videoMetadata");
    
    private final Settings settings;
    private final EventQueue events;
    
    private final ObjectMapper objectMapper;
    
    private final File cacheDir;
    private final LoadingCache<File, VideoMetadata> metadataMapping;
    
    @Inject
    public VideoMetadataReader(Settings settings, EventQueue events)
    {
        this.settings = settings;
        this.events = events;
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        
        cacheDir = CACHE_PATH.toFile();
        
        metadataMapping = CacheBuilder.newBuilder()
                                      .maximumSize(100)
                                      .expireAfterWrite(1, TimeUnit.DAYS)
                                      .build(new CacheLoader<>()
                                      {
                                          @Override
                                          public VideoMetadata load(File key) throws Exception
                                          {
                                              // check if directory exists on load because SETTINGS_DIR doesn't exist during construction on first launch
                                              if(!cacheDir.exists())
                                              {
                                                  if(cacheDir.mkdir())
                                                  {
                                                      cacheDir.setReadable(true);
                                                      cacheDir.setWritable(true);
                                                  }
                                                  else
                                                  {
                                                      events.postEvent(new Event(EventType.FAILURE,
                                                                                 "Failed to create metadata cache"));
                                                      return new VideoMetadata();
                                                  }
                                              }
                
                                              File cacheFile = findCacheFile(key);
                                              if(cacheFile.exists())
                                              {
                                                  VideoMetadata metadata = objectMapper.readValue(cacheFile,
                                                                                                  VideoMetadata.class);
                                                  if(!needsReparse(metadata))
                                                  {
                                                      return metadata;
                                                  }
                                              }
                
                                              return parseVideo(key);
                                          }
                                      });
    }
    
    public DateTime getCreationDate(File video)
    {
        if(video != null && video.exists())
        {
            try
            {
                return metadataMapping.get(video).getCreationDate();
            }
            catch(ExecutionException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "cache load failed " + e.getMessage()));
            }
        }
        return new DateTime(0);
    }
    
    public long getDuration(File video)
    {
        if(video != null && video.exists())
        {
            try
            {
                return metadataMapping.get(video).getDuration();
            }
            catch(ExecutionException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "cache load failed " + e.getMessage()));
            }
        }
        return -1;
    }
    
    public String getResolution(File video)
    {
        if(video != null && video.exists())
        {
            try
            {
                return metadataMapping.get(video).getResolution();
            }
            catch(ExecutionException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "cache load failed " + e.getMessage()));
            }
        }
        return "N/A";
    }
    
    public Image getThumbnail(File video)
    {
        return null;
    }
    
    private VideoMetadata parseVideo(File video)
    {
        if(video != null && video.exists())
        {
            try
            {
                VideoMetadata metadata = new VideoMetadata();
                JsonNode ffmpegJson = getMetadataJson(video);
                metadata.setCreationDate(new DateTime(video.lastModified()));
                if(!ffmpegJson.get("format").has("duration"))
                {
                    metadata.setDuration(-1);
                }
                else
                {
                    double durationSeconds = ffmpegJson.get("format").get("duration").asDouble();
                    metadata.setDuration((long) (durationSeconds * 1000));
                }
                int width = ffmpegJson.get("streams").get(0).get("width").asInt();
                int height = ffmpegJson.get("streams").get(0).get("height").asInt();
                metadata.setResolution(String.format("%dx%d", width, height));
                objectMapper.writeValue(findCacheFile(video), metadata);
                return metadata;
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Couldn't read metadata for " + video.getName()));
                events.postEvent(new Event(EventType.DEBUG, e.getMessage()));
            }
        }
        
        return new VideoMetadata();
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
    
    private File findCacheFile(File video)
    {
        Path original = Paths.get(video.toURI());
        String name = original.getParent().getFileName() + "_" + original.getFileName()
                                                                         .toString()
                                                                         .replace('.', '_') + ".json";
        return CACHE_PATH.resolve(name).toFile();
    }
    
    private boolean needsReparse(VideoMetadata metadata)
    {
        // if the video was an in-progress recording when parsed, the duration won't have been set yet
        return metadata.getDuration() == -1;
    }
}
