/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.video.VideoMetadataHandler;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Collections;
import java.util.Set;

public abstract class VideoInfoPanel extends DefaultPanel implements EventConsumer
{
    protected final VideoMetadataHandler metadataHandler;
    protected final EventQueue eventQueue;
    
    protected final JLabel thumbnailLabel;
    protected final ImageIcon thumbnailImage;
    
    protected File currentVideo;
    
    public VideoInfoPanel(VideoMetadataHandler metadataHandler, EventQueue eventQueue)
    {
        this.metadataHandler = metadataHandler;
        this.eventQueue = eventQueue;
    
        thumbnailLabel = new JLabel();
        thumbnailImage = new ImageIcon();
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e)
            {
                if(thumbnailLabel.getIcon() != null)
                {
                    setScaledThumbnail(currentVideo);
                }
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
        boolean thumbnailSet = setScaledThumbnail(video);
    
        if(!thumbnailSet)
        {
            thumbnailLabel.setIcon(null);
            thumbnailLabel.setText("Loading thumbnail...");
        }
    }
    
    private boolean setScaledThumbnail(File video)
    {
        Image image = metadataHandler.getThumbnail(video);
        if(image != null)
        {
            int imageWidth = (int)(Math.min(getWidth() * 0.9, 270));
            Image scaledImage = image.getScaledInstance(imageWidth, (int)(imageWidth*9./16), Image.SCALE_SMOOTH);
            thumbnailImage.setImage(scaledImage);
            thumbnailLabel.setIcon(thumbnailImage);
            thumbnailLabel.setText("");
            repaint();
            return true;
        }
        else
        {
            eventQueue.postEvent(new Event(EventType.WARNING, "Could not load thumbnail for " + video.getName()));
        }
        
        return false;
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
