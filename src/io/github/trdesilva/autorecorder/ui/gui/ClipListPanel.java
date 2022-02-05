package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
        videoListPanel = new VideoListPanel(new File(settings.getClipPath()), clipInfoPanel);
        
        add(videoListPanel, "grow");
        add(clipInfoPanel);
    }
    
    public void update()
    {
        videoListPanel.updateList();
    }
}
