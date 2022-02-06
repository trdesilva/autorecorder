/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import java.io.File;

public class ClipListPanel extends JPanel
{
    private Settings settings;
    
    private VideoListPanel videoListPanel;
    
    public ClipListPanel(Settings settings)
    {
        this.settings = settings;
        
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%:25%:300]"));
        
        ClipInfoPanel clipInfoPanel = new ClipInfoPanel();
    
        // TODO error handling
        File clipDir = null;
        if(settings.getRecordingPath() != null)
        {
            clipDir = new File(settings.getRecordingPath());
        }

        videoListPanel = new VideoListPanel(clipDir, clipInfoPanel);
        
        add(videoListPanel, "grow");
        add(clipInfoPanel);
    }
    
    public void update()
    {
        if(settings.getClipPath() != null)
        {
            videoListPanel.setVideoDir(new File(settings.getClipPath()));
        }
        else
        {
            videoListPanel.setVideoDir(null);
        }
    }
}
