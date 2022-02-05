/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.io.File;

public class RecordingListPanel extends JPanel
{
    private Settings settings;
    
    private VideoListPanel videoListPanel;
    
    public RecordingListPanel(Settings settings)
    {
        this.settings = settings;
    
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%:25%:300]"));
        
        RecordingInfoPanel infoPanel = new RecordingInfoPanel();
        // TODO error handling
        videoListPanel = new VideoListPanel(new File(settings.getRecordingPath()), infoPanel);
        
        add(videoListPanel, "grow");
        add(infoPanel);
    }
    
    public void update()
    {
        videoListPanel.updateList();
    }
}
