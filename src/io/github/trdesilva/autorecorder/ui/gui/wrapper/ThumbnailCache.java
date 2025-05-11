/*
 * Copyright (c) 2025 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.wrapper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;

import javax.annotation.CheckForNull;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@Singleton
public class ThumbnailCache implements LoadingCache<String, Image>
{
    private LoadingCache<String, Image> impl;
    
    @Inject
    public ThumbnailCache(EventQueue eventQueue)
    {
        impl = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<String, Image>() {
            @Override
            public Image load(String key) throws Exception
            {
                if(!key.isBlank())
                {
                    eventQueue.postEvent(new Event(EventType.DEBUG, String.format("Loading thumbnail from %s", key)));
                    File thumbnailFile = new File(key);
                    if(thumbnailFile.exists())
                    {
                        Image image = ImageIO.read(thumbnailFile);
                    
                        // image width is -1 while image is loading
                        int loadingWaitCounter = 0;
                        while(image.getWidth(null) == -1 && loadingWaitCounter++ < 10)
                        {
                            Thread.sleep(50);
                        }
                    
                        return image;
                    }
                }
                return null;
            }
        });
    }
    
    @Override
    public Image get(String key) throws ExecutionException
    {
        return impl.get(key);
    }
    
    @Override
    public Image getUnchecked(String key)
    {
        return impl.getUnchecked(key);
    }
    
    @Override
    public ImmutableMap<String, Image> getAll(Iterable<? extends String> keys) throws ExecutionException
    {
        return impl.getAll(keys);
    }
    
    @Override
    public Image apply(String key)
    {
        return impl.apply(key);
    }
    
    @Override
    public void refresh(String key)
    {
        impl.refresh(key);
    }
    
    @CheckForNull
    @Override
    public Image getIfPresent(Object key)
    {
        return impl.getIfPresent(key);
    }
    
    @Override
    public Image get(String key, Callable<? extends Image> loader) throws ExecutionException
    {
        return impl.get(key, loader);
    }
    
    @Override
    public ImmutableMap<String, Image> getAllPresent(Iterable<?> keys)
    {
        return impl.getAllPresent(keys);
    }
    
    @Override
    public void put(String key, Image value)
    {
        impl.put(key, value);
    }
    
    @Override
    public void putAll(Map<? extends String, ? extends Image> m)
    {
        impl.putAll(m);
    }
    
    @Override
    public void invalidate(Object key)
    {
        impl.invalidate(key);
    }
    
    @Override
    public void invalidateAll(Iterable<?> keys)
    {
        impl.invalidateAll(keys);
    }
    
    @Override
    public void invalidateAll()
    {
        impl.invalidateAll();
    }
    
    @Override
    public long size()
    {
        return impl.size();
    }
    
    @Override
    public CacheStats stats()
    {
        return impl.stats();
    }
    
    @Override
    public ConcurrentMap<String, Image> asMap()
    {
        return impl.asMap();
    }
    
    @Override
    public void cleanUp()
    {
        impl.cleanUp();
    }
}
