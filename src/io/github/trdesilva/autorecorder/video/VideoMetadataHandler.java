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
import io.github.trdesilva.autorecorder.clip.FfmpegHelper;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.ThumbnailCache;
import org.joda.time.DateTime;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Singleton
public class VideoMetadataHandler
{
    private static final Path CACHE_PATH = Settings.SETTINGS_DIR.resolve("videoMetadata");
    
    private final Settings settings;
    private final EventQueue events;
    private final FfmpegHelper ffmpegHelper;
    private final ThumbnailCache thumbnailCache;
    
    private final ObjectMapper objectMapper;
    
    private final File cacheDir;
    private final LoadingCache<File, VideoMetadata> metadataMapping;
    private final Map<File, ReentrantLock> metadataLocks;
    
    @Inject
    public VideoMetadataHandler(Settings settings, EventQueue events, FfmpegHelper ffmpegHelper, ThumbnailCache thumbnailCache)
    {
        this.settings = settings;
        this.events = events;
        this.ffmpegHelper = ffmpegHelper;
        this.thumbnailCache = thumbnailCache;
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        
        cacheDir = CACHE_PATH.toFile();
        metadataLocks = new ConcurrentHashMap<>();
        
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
                                                  ReentrantLock lock = getMetadataLock(key);
                                                  lock.lock();
                                                  try
                                                  {
                                                      VideoMetadata metadata = objectMapper.readValue(cacheFile,
                                                                                                      VideoMetadata.class);
                                                      if(!needsReparse(metadata))
                                                      {
                                                          return metadata;
                                                      }
                                                  }
                                                  finally
                                                  {
                                                      lock.unlock();
                                                  }
                                              }
                
                                              return parseVideo(key);
                                          }
                                      });
    }
    
    public DateTime getCreationDate(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata.getCreationDate(), new DateTime(0));
    }
    
    public long getDuration(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata.getDuration(), Long.valueOf(-1));
    }
    
    public String getResolution(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata.getResolution(), "N/A");
    }
    
    public List<Long> getBookmarks(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata.getBookmarks(), Collections.emptyList());
    }
    
    public String getGameName(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata.getGameName(), "N/A");
    }
    
    public String getThumbnailPath(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata.getThumbnailPath(), "");
    }
    
    public Image getThumbnail(File video)
    {
        String thumbnailPath = getMetadata(video).getThumbnailPath();
        if(!thumbnailPath.isBlank() && new File(thumbnailPath).exists())
        {
            try
            {
                return thumbnailCache.get(thumbnailPath);
            }
            catch(ExecutionException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Could not load thumbnail for " + video.getName()));
            }
        }
        
        return null;
    }
    
    public Image getThumbnail(File video, boolean block)
    {
        String thumbnailPath = getMetadata(video).getThumbnailPath();
        if(!thumbnailPath.isBlank() && new File(thumbnailPath).exists())
        {
            try
            {
                if(block)
                {
                    return thumbnailCache.get(thumbnailPath);
                }
                else
                {
                    return thumbnailCache.getIfPresent(thumbnailPath);
                }
            }
            catch(ExecutionException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Could not load thumbnail for " + video.getName()));
            }
        }
        
        return null;
    }
    
    public VideoMetadata getMetadata(File video)
    {
        return fetchMetadataItem(video, metadata -> metadata, new VideoMetadata());
    }
    
    public void saveMetadata(File video, VideoMetadata metadata)
    {
        if(video != null && video.exists() && metadata != null)
        {
            ReentrantLock lock = getMetadataLock(video);
            lock.lock();
            try
            {
                objectMapper.writeValue(findCacheFile(video), metadata);
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.DEBUG,
                                           String.format("failed to save metadata %s for file %s: %s", metadata,
                                                         video.getName(), e.getMessage())));
            }
            finally
            {
                lock.unlock();
            }
            metadataMapping.put(video, metadata);
            metadataMapping.invalidate(video);
        }
    }
    
    public void deleteMetadata(File video)
    {
        if(video != null && !video.exists())
        {
            ReentrantLock lock = getMetadataLock(video);
            lock.lock();
            try
            {
                events.postEvent(new Event(EventType.DEBUG,
                                           "deleting metadata for file " + video.getName()));
                
                if(!findCacheFile(video).delete())
                {
                    events.postEvent(new Event(EventType.DEBUG,
                                               String.format("failed to delete metadata for file %s", video.getName())));
                }
                
                if(!findThumbnail(video).delete())
                {
                    events.postEvent(new Event(EventType.DEBUG,
                                               String.format("failed to delete thumbnail for file %s", video.getName())));
                }
            }
            finally
            {
                lock.unlock();
            }
            metadataMapping.invalidate(video);
        }
    }
    
    public void saveBookmark(File recording, long timestamp)
    {
        ReentrantLock lock = getMetadataLock(recording);
        lock.lock();
        try
        {
            VideoMetadata metadata = getMetadata(recording);
            metadata.getBookmarks().add(timestamp);
            saveMetadata(recording, metadata);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    private <T> T fetchMetadataItem(File video, Function<VideoMetadata, T> metadataGetter, T defaultValue)
    {
        if(video != null && video.exists())
        {
            try
            {
                VideoMetadata metadata = metadataMapping.get(video);
                if(needsReparse(metadata))
                {
                    metadataMapping.invalidate(video);
                }
                return metadataGetter.apply(metadata);
            }
            catch(ExecutionException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "cache load failed " + e.getMessage()));
            }
        }
        return defaultValue;
    }
    
    private ReentrantLock getMetadataLock(File video)
    {
        return metadataLocks.computeIfAbsent(video, f -> new ReentrantLock());
    }
    
    private synchronized VideoMetadata parseVideo(File video)
    {
        if(video != null && video.exists())
        {
            ReentrantLock lock = getMetadataLock(video);
            lock.lock();
            try
            {
                File currentMetadataFile = findCacheFile(video);
                VideoMetadata metadata;
                if(currentMetadataFile.exists())
                {
                    metadata = objectMapper.readValue(currentMetadataFile, VideoMetadata.class);
                }
                else
                {
                    metadata = new VideoMetadata();
                }
                JsonNode ffmpegJson = getMetadataJson(video);
                metadata.setCreationDate(new DateTime(video.lastModified()));
                
                int width;
                int height;
                if(!ffmpegJson.has("streams") || !ffmpegJson.get("streams").has(0)
                        || !(ffmpegJson.get("streams").get(0).has("width") && ffmpegJson.get("streams")
                                                                                        .get(0)
                                                                                        .has("height")))
                {
                    width = 0;
                    height = 0;
                    metadata.setResolution("N/A");
                }
                else
                {
                    width = ffmpegJson.get("streams").get(0).get("width").asInt();
                    height = ffmpegJson.get("streams").get(0).get("height").asInt();
                    metadata.setResolution(String.format("%dx%d", width, height));
                }
    
                if(!ffmpegJson.has("format") || !ffmpegJson.get("format").has("duration"))
                {
                    metadata.setDuration(-1);
                }
                else
                {
                    // this is a proxy for the recording being complete, so we can generate a thumbnail now too
                    double durationSeconds = ffmpegJson.get("format").get("duration").asDouble();
                    metadata.setDuration((long) (durationSeconds * 1000));
    
                    String thumbnailPath = findThumbnail(video).getAbsolutePath();
                    new Thread(() -> {
                        try
                        {
                            ffmpegHelper.runFfmpeg(
                                    new LinkedList<>(Arrays.asList("-ss",
                                                                   String.format("%d", (int) Math.min(600, durationSeconds / 2)),
                                                                   "-i",
                                                                   video.getAbsolutePath(),
                                                                   "-frames:v",
                                                                   "1",
                                                                   "-vf",
                                                                   String.format("thumbnail=n=120,scale=%d:%d",
                                                                                 width != 0 ? width / 6 : 320,
                                                                                 height != 0 ? height / 6 : 180),
                                                                   thumbnailPath
                                                                  )));
                            events.postEvent(new Event(EventType.THUMBNAIL_GENERATED,
                                                       "Thumbnail generated for " + video.getAbsolutePath(),
                                                       Map.of(EventProperty.THUMBNAIL_SOURCE, video)));
                        }
                        catch(IOException|InterruptedException e)
                        {
                            events.postEvent(new Event(EventType.WARNING, "Couldn't create thumbnail for " + video.getName()));
                            events.postEvent(new Event(EventType.DEBUG, e.getMessage()));
                        }
                    }).start();
                    metadata.setThumbnailPath(thumbnailPath);
                }
                
                objectMapper.writeValue(currentMetadataFile, metadata);
                return metadata;
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Couldn't read metadata for " + video.getName()));
                events.postEvent(new Event(EventType.DEBUG, e.getMessage()));
            }
            finally
            {
                lock.unlock();
            }
        }
        
        return new VideoMetadata();
    }
    
    private JsonNode getMetadataJson(File video) throws IOException
    {
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
    
    private File findThumbnail(File video)
    {
        Path original = Paths.get(video.toURI());
        String name = original.getParent().getFileName() + "_" + original.getFileName()
                                                                         .toString()
                                                                         .replace('.', '_') + ".jpg";
        return CACHE_PATH.resolve(name).toFile();
    }
    
    private boolean needsReparse(VideoMetadata metadata)
    {
        // if the video was an in-progress recording when parsed, the duration won't have been set yet
        return metadata.getDuration() == -1 || metadata.getResolution().equals("N/A") || metadata.getThumbnailPath().isBlank();
    }
}
