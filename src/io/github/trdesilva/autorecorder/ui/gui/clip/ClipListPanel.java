/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

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

public class ClipListPanel extends DefaultPanel
{
    private final ClipInfoPanel clipInfoPanel;
    private final VideoListPanel videoListPanel;
    private final JRadioButton dateSortButton;
    private final JRadioButton nameSortButton;
    
    @Inject
    public ClipListPanel(ClipInfoPanel clipInfoPanel, VideoListPanelFactory videoListPanelFactory,
                         @Named("CLIP") VideoListHandler clipListHandler)
    {
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%:25%:300]", "[][grow]"));
        
        this.clipInfoPanel = clipInfoPanel;
        videoListPanel = videoListPanelFactory.create(clipListHandler, clipInfoPanel);
        
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
        add(clipInfoPanel, "cell 1 0, grow, span 1 2");
    
        dateSortButton.addActionListener(e -> update(false));
        nameSortButton.addActionListener(e -> update(false));
        
        update(false);
    }
    
    public void update(boolean clearSelection)
    {
        if(clearSelection)
        {
            clipInfoPanel.setVideo(null);
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
