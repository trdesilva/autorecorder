package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Main;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;

public class ClipInfoPanel extends JPanel implements VideoListSelectionConsumer
{
    private WrappingLabel title;
    private JLabel duration;
    private JLabel resolution;
    
    private JButton uploadButton;
    
    private File clip;
    
    public ClipInfoPanel()
    {
        title = new WrappingLabel("");
        title.setFont(new Font(null, Font.BOLD, 14));
        duration = new JLabel("");
        resolution = new JLabel("");
        
        uploadButton = new JButton("Upload...");
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(e -> {
            if(clip != null)
            {
                MainWindow.getInstance().showUploadView(clip);
            }
        });
    
        setLayout(new GridLayout(4, 1));
        
        add(title);
        add(duration);
        add(resolution);
        add(uploadButton);
        
        setPreferredSize(new Dimension(MainWindow.PREFERRED_WIDTH/4, MainWindow.PREFERRED_HEIGHT));
    }
    
    public void setVideo(File video)
    {
        this.clip = video;
        
        if(video != null)
        {
            title.setText(video.getName());
            duration.setText("Duration: ");
            resolution.setText("Resolution: ");
            uploadButton.setEnabled(true);
        }
    }
}
