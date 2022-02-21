/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.inject.VideoListPanelFactory;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.video.VideoType;
import net.miginfocom.swing.MigLayout;

public class RecordingListPanel extends DefaultPanel
{
    private final VideoListPanel videoListPanel;
    
    @Inject
    public RecordingListPanel(RecordingInfoPanel recordingInfoPanel,
                              VideoListPanelFactory videoListPanelFactory)
    {
        setLayout(new MigLayout("fill, insets 2", "[75%:75%:90%]2[10%::300]"));
        
        videoListPanel = videoListPanelFactory.create(VideoType.RECORDING, recordingInfoPanel);
        
        add(videoListPanel, "grow");
        add(recordingInfoPanel, "grow");
    }
    
    public void update()
    {
        videoListPanel.updateList();
    }
}
