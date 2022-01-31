package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;

public class ClipListPanel extends JPanel
{
    private Settings settings;
    
    public ClipListPanel(Settings settings)
    {
        this.settings = settings;
    
        BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(layout);
        
        ClipInfoPanel clipInfoPanel = new ClipInfoPanel();
    
        // TODO error handling
        add(new VideoListPanel(new File(settings.getClipPath()), clipInfoPanel));
        
        add(clipInfoPanel);
    }
}
