/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.clip.VideoMetadataReader;
import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;

public class RecordingInfoPanel extends JPanel implements VideoListSelectionConsumer
{
    public static final String RECORDING_TO_CLIP_PROP = "recordingToClip";
    
    private WrappingLabel title;
    private WrappingLabel creationDate;
    private JLabel duration;
    private JLabel resolution;
    
    private JButton clipButton;
    
    private File recording;
    private VideoMetadataReader metadataReader;
    
    public RecordingInfoPanel(Settings settings)
    {
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
                System.out.println("firing clipper start " + recording);
                if(recording != null)
                {
                    MainWindow.getInstance().showClipView(recording);
                }
            }
        });
        
        metadataReader = new VideoMetadataReader(settings);
        
        add(title, "cell 0 0, growx, top");
        add(creationDate, "cell 0 1, , growx");
        add(duration, "cell 0 2, growx");
        add(resolution, "cell 0 3, growx");
        add(clipButton, "cell 0 4, growx, bottom");
    }
    
    public void setVideo(File video)
    {
        this.recording = video;
        
        if(video != null)
        {
            title.setText(video.getName());
            creationDate.setText("Created: " + metadataReader.getCreationDate(video));
            duration.setText("Duration: " + formatTime(metadataReader.getDuration(video)));
            resolution.setText("Resolution: " + metadataReader.getResolution(video));
            clipButton.setEnabled(true);
        }
    }
}