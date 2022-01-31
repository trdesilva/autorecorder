package io.github.trdesilva.autorecorder.ui.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.io.File;

public class ClippingPanel extends JPanel
{
    VideoPlaybackPanel playbackPanel;
    
    public ClippingPanel()
    {
        setLayout(new BorderLayout());
        
        playbackPanel = new VideoPlaybackPanel();
        
        add(playbackPanel, BorderLayout.CENTER);
    }
    
    public void setRecording(File videoFile)
    {
        playbackPanel.play(videoFile);
    }
}
