package io.github.trdesilva.autorecorder.ui.gui;

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
    public VideoListPanel(File videoDir, VideoListSelectionConsumer selectionConsumer)
    {
        // TODO filename filter for only videos/error hadnling
        JList<File> videos = new JList<>(videoDir.listFiles());
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
        
        setPreferredSize(new Dimension(3*MainWindow.PREFERRED_WIDTH/4, MainWindow.PREFERRED_HEIGHT));
    }
}
