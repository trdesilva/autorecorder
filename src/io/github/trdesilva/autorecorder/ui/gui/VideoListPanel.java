/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import javax.annotation.Nullable;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;

public class VideoListPanel extends JScrollPane
{
    private final StatusQueue status;
    private File videoDir;
    private final JList<File> videos;
    
    @AssistedInject
    public VideoListPanel(StatusQueue status, @Assisted @Nullable File videoDir,
                          @Assisted VideoListSelectionConsumer selectionConsumer)
    {
        this.status = status;
        
        // TODO filename filter for only videos/error hadnling
        // TODO custom renderer with thumbnails
        // TODO update file list
        this.videoDir = videoDir;
        if(videoDir != null && videoDir.exists() && videoDir.isDirectory())
        {
            videos = new JList<>(videoDir.listFiles());
        }
        else
        {
            videos = new JList<>(new File[0]);
        }
        videos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
    }
    
    public void updateList()
    {
        if(videoDir != null && videoDir.exists() && videoDir.isDirectory())
        {
            videos.setListData(videoDir.listFiles());
        }
        else
        {
            status.postMessage(new StatusMessage(StatusType.WARNING,
                                                 "Couldn't find videos; please update recording/clip path in settings"));
        }
    }
    
    public void setVideoDir(File videoDir)
    {
        this.videoDir = videoDir;
        updateList();
    }
}
