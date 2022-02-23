/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.inject.VideoListPanelFactory;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class RecordingListPanel extends DefaultPanel
{
    private final VideoListPanel videoListPanel;
    private final JRadioButton dateSortButton;
    private final JRadioButton nameSortButton;
    
    @Inject
    public RecordingListPanel(RecordingInfoPanel recordingInfoPanel, VideoListPanelFactory videoListPanelFactory,
                              @Named("RECORDING") VideoListHandler recordingListHandler)
    {
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%::300]", "[][grow]"));
        
        videoListPanel = videoListPanelFactory.create(recordingListHandler, recordingInfoPanel);
        
        JPanel sortPanel = new JPanel();
        sortPanel.setLayout(new MigLayout());
        JLabel sortLabel = new JLabel("Sort by:");
        ButtonGroup sortButtons = new ButtonGroup();
        dateSortButton = new JRadioButton("Most recent", true);
        nameSortButton = new JRadioButton("Alphabetical");
        sortButtons.add(dateSortButton);
        sortButtons.add(nameSortButton);
    
        sortPanel.add(sortLabel);
        sortPanel.add(dateSortButton);
        sortPanel.add(nameSortButton);
    
        add(sortPanel, "cell 0 0, left");
        add(videoListPanel, "cell 0 1, grow");
        add(recordingInfoPanel, "cell 1 0, grow, span 1 2");
        
        dateSortButton.addActionListener(e -> update());
        nameSortButton.addActionListener(e -> update());
        
        update();
    }
    
    public void update()
    {
        if(dateSortButton.isSelected())
        {
            videoListPanel.updateList(VideoListPanel.SortOrder.DATE);
        }
        else if(nameSortButton.isSelected())
        {
            videoListPanel.updateList(VideoListPanel.SortOrder.NAME);
        }
        else
        {
            videoListPanel.updateList(null);
        }
    }
}
