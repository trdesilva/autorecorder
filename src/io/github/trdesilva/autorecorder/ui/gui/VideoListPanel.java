/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.io.File;

public class VideoListPanel extends JScrollPane
{
    private File videoDir;
    private final JList<File> videos;
    
    public VideoListPanel(File videoDir, VideoListSelectionConsumer selectionConsumer)
    {
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
                if(e.getValueIsAdjusting()) return;
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
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Couldn't find videos; please update recording/clip path in settings"));
        }
    }
    
    public void setVideoDir(File videoDir)
    {
        this.videoDir = videoDir;
        updateList();
    }
}
