/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.video.VideoMetadataHandler;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Image;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public abstract class VideoInfoPanel extends DefaultPanel implements EventConsumer
{
    protected final VideoMetadataHandler metadataHandler;
    protected final EventQueue eventQueue;
    
    protected final JLabel thumbnailLabel;
    protected final ImageIcon thumbnailImage;
    protected final LoadingCache<String, Image> thumbnailCache;
    
    protected File currentVideo;
    
    public VideoInfoPanel(VideoMetadataHandler metadataHandler, EventQueue eventQueue)
    {
        this.metadataHandler = metadataHandler;
        this.eventQueue = eventQueue;
    
        thumbnailLabel = new JLabel();
        thumbnailImage = new ImageIcon();
        
        thumbnailCache = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<String, Image>() {
            @Override
            public Image load(String key) throws Exception
            {
                if(!key.isBlank())
                {
                    eventQueue.postEvent(new Event(EventType.DEBUG, String.format("Loading thumbnail for %s from %s", currentVideo.getAbsolutePath(), key)));
                    File thumbnailFile = new File(key);
                    if(thumbnailFile.exists())
                    {
                        Image image = ImageIO.read(thumbnailFile).getScaledInstance((int) (getWidth()*0.9), (int)(getWidth()*0.9*9./16), Image.SCALE_SMOOTH);
                    
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
        
        eventQueue.addConsumer(this);
    }
    
    public void setVideo(File video)
    {
        currentVideo = video;
        updateUi(video);
    }
    
    protected abstract void updateUi(File video);
    
    protected void loadThumbnail(File video)
    {
        boolean thumbnailSet = false;
        String thumbnailPath = metadataHandler.getMetadata(video).getThumbnailPath();
        if(!thumbnailPath.isBlank() && new File(thumbnailPath).exists())
        {
            try
            {
                Image image = thumbnailCache.get(thumbnailPath);
                if(image != null)
                {
                    thumbnailImage.setImage(image);
                    thumbnailLabel.setIcon(thumbnailImage);
                    repaint();
                    thumbnailSet = true;
                }
                else
                {
                    eventQueue.postEvent(new Event(EventType.WARNING, "Could not load thumbnail for " + video.getName()));
                }
            }
            catch(ExecutionException e)
            {
                eventQueue.postEvent(new Event(EventType.WARNING, "Could not load thumbnail for " + video.getName()));
            }
        }
    
        if(!thumbnailSet)
        {
            thumbnailLabel.setIcon(null);
            thumbnailLabel.setText("Loading thumbnail...");
        }
    }
    
    @Override
    public void post(Event event)
    {
        if(event.getType().equals(EventType.THUMBNAIL_GENERATED))
        {
            File thumbnailSource = (File) event.getProperties().get(EventProperty.THUMBNAIL_SOURCE);
            if(thumbnailSource.getAbsolutePath().equals(currentVideo.getAbsolutePath()))
            {
                loadThumbnail(thumbnailSource);
            }
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return Collections.singleton(EventType.THUMBNAIL_GENERATED);
    }
}
