/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import java.io.File;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;

public class ClipInfoPanel extends DefaultPanel implements VideoListSelectionConsumer
{
    private final VideoListHandler clipListHandler;
    
    private final WrappingLabel title;
    private final JLabel duration;
    private final JLabel resolution;
    
    private final JButton uploadButton;
    
    private File clip;
    
    @Inject
    public ClipInfoPanel(@Named("CLIP") VideoListHandler clipListHandler, Navigator navigator)
    {
        this.clipListHandler = clipListHandler;
        
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
        
        add(title, "cell 0 0, growx, wmin 100");
        add(duration, "cell 0 1, growx, wmin 100");
        add(resolution, "cell 0 2, growx, wmin 100");
        add(uploadButton, "cell 0 3, growx, wmin 100, tag next");
    }
    
    public void setVideo(File video)
    {
        this.clip = video;
        
        if(video != null)
        {
            title.setText(video.getName());
            duration.setText("Duration: " + formatTime(clipListHandler.getDuration(video)));
            resolution.setText("Resolution: " + clipListHandler.getResolution(video));
            uploadButton.setEnabled(true);
        }
        else
        {
            title.setText("");
            duration.setText("");
            resolution.setText("");
            uploadButton.setEnabled(false);
        }
    }
}
