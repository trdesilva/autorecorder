/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventConsumer;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoListPanel extends JScrollPane implements EventConsumer
{
    public enum SortOrder
    {
        NAME,
        DATE
    }
    
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.RECORDING_END);
    
    private final EventQueue events;
    private VideoListHandler videoListHandler;
    
    private final JList<File> videos;
    private SortOrder sortOrder;
    
    @AssistedInject
    public VideoListPanel(EventQueue events, @Assisted VideoListHandler videoListHandler, @Assisted VideoListSelectionConsumer selectionConsumer)
    {
        this.events = events;
        
        // TODO custom renderer with thumbnails
        this.videoListHandler = videoListHandler;
        videos = new JList<>(videoListHandler.getVideoList().toArray(new File[0]));
        videos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        sortOrder = SortOrder.DATE;
        
        videos.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if(e.getValueIsAdjusting())
                {
                    return;
                }
                selectionConsumer.setVideo(videos.getSelectedValue());
            }
        });
        getViewport().add(videos);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        events.addConsumer(this);
    }
    
    public void updateList(SortOrder sortOrder)
    {
        List<File> videoList = videoListHandler.getVideoList();
        if(sortOrder != null)
        {
            this.sortOrder = sortOrder;
            switch(sortOrder)
            {
                case NAME:
                    videoList = videoList.stream()
                                         .sorted(Comparator.comparing(File::getName))
                                         .collect(Collectors.toList());
                    break;
                case DATE:
                    videoList = videoList.stream()
                                         .sorted(Comparator.comparing(File::lastModified).reversed())
                                         .collect(Collectors.toList());
                    break;
            }
        }
        File[] videoArray = videoList.toArray(new File[0]);
        videos.setListData(videoArray);
    }
    
    @Override
    public void post(Event event)
    {
        if(event.getType().equals(EventType.RECORDING_END))
        {
            SwingUtilities.invokeLater(() -> updateList(sortOrder));
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
}
