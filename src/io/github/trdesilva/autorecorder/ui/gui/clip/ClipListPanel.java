/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.inject.VideoListPanelFactory;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import net.miginfocom.swing.MigLayout;

import java.io.File;

public class ClipListPanel extends DefaultPanel
{
    private final Settings settings;
    
    private final VideoListPanel videoListPanel;
    
    @Inject
    public ClipListPanel(Settings settings, ClipInfoPanel clipInfoPanel, VideoListPanelFactory videoListPanelFactory)
    {
        this.settings = settings;
        
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%:25%:300]"));
        
        // TODO error handling
        // TODO #3 replace direct access with clip handler
        File clipDir = null;
        if(settings.getRecordingPath() != null)
        {
            clipDir = new File(settings.getRecordingPath());
        }
        
        videoListPanel = videoListPanelFactory.create(clipDir, clipInfoPanel);
        
        add(videoListPanel, "grow");
        add(clipInfoPanel, "grow");
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
