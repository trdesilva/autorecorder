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
    private final RecordingInfoPanel recordingInfoPanel;
    private final VideoListPanel videoListPanel;
    private final JRadioButton dateSortButton;
    private final JRadioButton nameSortButton;
    private final JRadioButton tileDisplayButton;
    private final JRadioButton nameDisplayButton;
    
    @Inject
    public RecordingListPanel(RecordingInfoPanel recordingInfoPanel, VideoListPanelFactory videoListPanelFactory,
                              @Named("RECORDING") VideoListHandler recordingListHandler)
    {
        MigLayout layout = new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%::300]", "[30][300:100%]");
        setLayout(layout);
    
        this.recordingInfoPanel = recordingInfoPanel;
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
    
        JPanel displayTypePanel = new JPanel();
        displayTypePanel.setLayout(new MigLayout());
        JLabel displayTypeLabel = new JLabel("Display as:");
        ButtonGroup displayTypeButtons = new ButtonGroup();
        tileDisplayButton = new JRadioButton("Tiles", true);
        nameDisplayButton = new JRadioButton("Filenames");
        displayTypeButtons.add(tileDisplayButton);
        displayTypeButtons.add(nameDisplayButton);
    
        displayTypePanel.add(displayTypeLabel);
        displayTypePanel.add(tileDisplayButton);
        displayTypePanel.add(nameDisplayButton);
    
        add(sortPanel, "cell 0 0, left, growx, split 2");
        add(displayTypePanel, "cell 0 0, growx, right");
        add(videoListPanel, "cell 0 1, growx");
        add(recordingInfoPanel, "cell 1 0, grow, span 1 2");
        
        dateSortButton.addActionListener(e -> update(false));
        nameSortButton.addActionListener(e -> update(false));
        
        tileDisplayButton.addActionListener(e -> videoListPanel.changeDisplayType(VideoListPanel.DisplayType.TILES));
        nameDisplayButton.addActionListener(e -> videoListPanel.changeDisplayType(VideoListPanel.DisplayType.NAMES));
        
        update(false);
    }
    
    public void update(boolean clearSelection)
    {
        if(clearSelection)
        {
            recordingInfoPanel.setVideo(null);
        }
        
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
