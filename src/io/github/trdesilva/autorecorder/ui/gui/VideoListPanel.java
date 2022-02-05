package io.github.trdesilva.autorecorder.ui.gui;

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
    private final File videoDir;
    private final JList<File> videos;
    
    public VideoListPanel(File videoDir, VideoListSelectionConsumer selectionConsumer)
    {
        // TODO filename filter for only videos/error hadnling
        // TODO custom renderer with thumbnails
        // TODO update file list
        this.videoDir = videoDir;
        videos = new JList<>(videoDir.listFiles());
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
        videos.setListData(videoDir.listFiles());
    }
}
