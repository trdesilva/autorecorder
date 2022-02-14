/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.clip.VideoMetadataReader;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import java.io.File;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;

public class ClipInfoPanel extends DefaultPanel implements VideoListSelectionConsumer
{
    private final VideoMetadataReader metadataReader;
    
    private final WrappingLabel title;
    private final JLabel duration;
    private final JLabel resolution;
    
    private final JButton uploadButton;
    
    private File clip;
    
    @Inject
    public ClipInfoPanel(VideoMetadataReader metadataReader, Navigator navigator)
    {
        this.metadataReader = metadataReader;
        
        setLayout(new MigLayout("fill", "[100:null:null]", "[][][]push[]"));
        
        title = new WrappingLabel("");
        title.setFont(new Font(null, Font.BOLD, 14));
        duration = new JLabel("");
        resolution = new JLabel("");
        
        uploadButton = new JButton("Upload...");
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(e -> {
            if(clip != null)
            {
                navigator.showUploadView(clip);
            }
        });
        
        add(title, "cell 0 0, growx");
        add(duration, "cell 0 1, growx");
        add(resolution, "cell 0 2, growx");
        add(uploadButton, "cell 0 3, growx, tag next");
    }
    
    public void setVideo(File video)
    {
        this.clip = video;
        
        if(video != null)
        {
            title.setText(video.getName());
            duration.setText("Duration: " + formatTime(metadataReader.getDuration(video)));
            resolution.setText("Resolution: " + metadataReader.getResolution(video));
            uploadButton.setEnabled(true);
        }
    }
}
