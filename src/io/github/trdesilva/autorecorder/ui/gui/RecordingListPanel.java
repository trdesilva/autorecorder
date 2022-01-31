package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.io.File;

public class RecordingListPanel extends JPanel
{
    private Settings settings;
    
    public RecordingListPanel(Settings settings)
    {
        this.settings = settings;
    
        BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layout);
        
        RecordingInfoPanel infoPanel = new RecordingInfoPanel();
        // TODO error handling
        add(new VideoListPanel(new File(settings.getRecordingPath()), infoPanel));
        
        add(infoPanel);
    }
}
