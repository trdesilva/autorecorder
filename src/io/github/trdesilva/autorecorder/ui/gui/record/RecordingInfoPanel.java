/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;

public class RecordingInfoPanel extends JPanel implements VideoListSelectionConsumer
{
    public static final String RECORDING_TO_CLIP_PROP = "recordingToClip";
    
    private WrappingLabel title;
    private WrappingLabel creationDate;
    private JLabel duration;
    private JLabel resolution;
    
    private JButton clipButton;
    
    private File recording;
    
    public RecordingInfoPanel()
    {
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
        
        setLayout(new GridLayout(4, 1));
        
        add(title);
        add(duration);
        add(resolution);
        add(clipButton);
    
        setPreferredSize(new Dimension(MainWindow.PREFERRED_WIDTH/4, MainWindow.PREFERRED_HEIGHT));
    }
    
    public void setVideo(File video)
    {
        this.recording = video;
        
        if(video != null)
        {
            title.setText(video.getName());
            creationDate.setText("Created: ");
            duration.setText("Duration: ");
            resolution.setText("Resolution: ");
            clipButton.setEnabled(true);
        }
    }
}