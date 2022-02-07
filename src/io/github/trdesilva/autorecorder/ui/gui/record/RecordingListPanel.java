/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import java.io.File;

public class RecordingListPanel extends JPanel
{
    private Settings settings;
    
    private VideoListPanel videoListPanel;
    
    public RecordingListPanel(Settings settings)
    {
        this.settings = settings;
    
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%::300]"));
        
        RecordingInfoPanel infoPanel = new RecordingInfoPanel(settings);
        // TODO error handling
        File recordingDir = null;
        if(settings.getRecordingPath() != null)
        {
            recordingDir = new File(settings.getRecordingPath());
        }

        videoListPanel = new VideoListPanel(recordingDir, infoPanel);
        
        add(videoListPanel, "grow");
        add(infoPanel, "grow");
    }
    
    public void update()
    {
        if(settings.getRecordingPath() != null)
        {
            videoListPanel.setVideoDir(new File(settings.getRecordingPath()));
        }
        else
        {
            videoListPanel.setVideoDir(null);
        }
    }
}
