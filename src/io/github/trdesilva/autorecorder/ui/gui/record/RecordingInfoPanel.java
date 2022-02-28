/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.clip.VideoMetadataReader;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;

public class RecordingInfoPanel extends DefaultPanel implements VideoListSelectionConsumer
{
    private final VideoMetadataReader metadataReader;
    
    private final WrappingLabel title;
    private final WrappingLabel creationDate;
    private final JLabel duration;
    private final JLabel resolution;
    
    private final JButton clipButton;
    
    private File recording;
    
    @Inject
    public RecordingInfoPanel(VideoMetadataReader metadataReader, Navigator navigator)
    {
        this.metadataReader = metadataReader;
        
        setLayout(new MigLayout("fill", "[100:null:null]", "[][][][]push[]"));
        
        title = new WrappingLabel("");
        title.setFont(new Font(null, Font.BOLD, 14));
        creationDate = new WrappingLabel("");
        duration = new JLabel();
        duration.setHorizontalAlignment(SwingConstants.LEFT);
        resolution = new JLabel();
        
        clipButton = new JButton("Clip...");
        clipButton.setEnabled(false);
        clipButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(recording != null)
                {
                    navigator.showClipView(recording);
                }
            }
        });
        
        add(title, "cell 0 0, growx, wmin 100");
        add(creationDate, "cell 0 1, growx, wmin 100");
        add(duration, "cell 0 2, growx, wmin 100");
        add(resolution, "cell 0 3, growx, wmin 100");
        add(clipButton, "cell 0 4, growx, wmin 100");
    }
    
    public void setVideo(File video)
    {
        this.recording = video;
        
        if(video != null)
        {
            title.setText(video.getName());
            creationDate.setText("Created: " + metadataReader.getCreationDate(video));
            long duration = metadataReader.getDuration(video);
            this.duration.setText("Duration: " + formatTime(duration));
            resolution.setText("Resolution: " + metadataReader.getResolution(video));
            if(duration != -1)
            {
                clipButton.setEnabled(true);
            }
            else
            {
                clipButton.setEnabled(false);
            }
        }
        else
        {
            title.setText("");
            creationDate.setText("");
            duration.setText("");
            resolution.setText("");
            clipButton.setEnabled(false);
        }
    }
}