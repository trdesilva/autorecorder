/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.inject.VideoListPanelFactory;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.video.VideoType;
import net.miginfocom.swing.MigLayout;

public class ClipListPanel extends DefaultPanel
{
    private final VideoListPanel videoListPanel;
    
    @Inject
    public ClipListPanel(ClipInfoPanel clipInfoPanel, VideoListPanelFactory videoListPanelFactory)
    {
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%:25%:300]"));
        
        videoListPanel = videoListPanelFactory.create(VideoType.CLIP, clipInfoPanel);
        
        add(videoListPanel, "grow");
        add(clipInfoPanel, "grow");
    }
    
    public void update()
    {
        videoListPanel.updateList();
    }
}
