/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

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
    private VideoListHandler videoListHandler;
    private final JList<File> videos;
    
    @AssistedInject
    public VideoListPanel(StatusQueue status, @Assisted VideoListHandler videoListHandler, @Assisted VideoListSelectionConsumer selectionConsumer)
    {
        this.status = status;
        
        // TODO custom renderer with thumbnails
        this.videoListHandler = videoListHandler;
        videos = new JList<>(videoListHandler.getVideoList().toArray(new File[0]));
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
        File[] videoList = videoListHandler.getVideoList().toArray(new File[0]);
        videos.setListData(videoList);
    }
}
