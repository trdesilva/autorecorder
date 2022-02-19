/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.clip.ClipJob;
import io.github.trdesilva.autorecorder.clip.ClipQueue;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoPlaybackPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.File;

import static io.github.trdesilva.autorecorder.TimestampUtil.parseTime;

public class ClippingPanel extends DefaultPanel
{
    private final VideoPlaybackPanel playbackPanel;
    private File videoFile;
    
    @Inject
    public ClippingPanel(VideoPlaybackPanel playbackPanel, ClipQueue clipQueue, StatusQueue status, Navigator navigator)
    {
        setLayout(new MigLayout("fill", "[grow]", "[30!][grow][]"));
        
        JButton backButton = new JButton("Back");
        
        this.playbackPanel = playbackPanel;
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new MigLayout("fill", "[][grow][][][80!][][][80!][][]", "[]"));
        
        JLabel titleLabel = new JLabel("Title");
        JLabel startTimeLabel = new JLabel("Start");
        JLabel endTimeLabel = new JLabel("End");
        
        JTextField titleField = new JTextField();
        JTextField startTimeField = new JTextField();
        startTimeField.setColumns(10);
        JTextField endTimeField = new JTextField();
        endTimeField.setColumns(10);
        
        JButton startCurrentTimeButton = new JButton("Now");
        JButton endCurrentTimeButton = new JButton("Now");
        JButton previewButton = new JButton("Preview");
        JButton saveButton = new JButton("Save");
        
        controlPanel.add(titleLabel, "cell 0 0");
        controlPanel.add(titleField, "cell 1 0, grow");
        controlPanel.add(startTimeLabel, "cell 2 0");
        controlPanel.add(startTimeField, "cell 3 0");
        controlPanel.add(startCurrentTimeButton, "cell 4 0");
        controlPanel.add(endTimeLabel, "cell 5 0");
        controlPanel.add(endTimeField, "cell 6 0");
        controlPanel.add(endCurrentTimeButton, "cell 7 0");
        controlPanel.add(previewButton, "cell 8 0");
        controlPanel.add(saveButton, "cell 9 0");
        
        add(backButton, "cell 0 0");
        add(playbackPanel, "cell 0 1, grow, wmin 400, hmin 300");
        add(controlPanel, "cell 0 2, growx");
        
        backButton.addActionListener(e -> {
            playbackPanel.stop();
            
            titleField.setText("");
            startTimeField.setText("");
            endTimeField.setText("");
            
            navigator.showMainView();
        });
        
        startCurrentTimeButton.addActionListener(e -> {
            startTimeField.setText(TimestampUtil.formatTime(playbackPanel.getPlaybackTime()));
        });
        
        endCurrentTimeButton.addActionListener(e -> {
            endTimeField.setText(TimestampUtil.formatTime(playbackPanel.getPlaybackTime()));
        });
        
        previewButton.addActionListener(e -> {
            long start = parseTime(startTimeField.getText());
            long end = parseTime(endTimeField.getText());
            if(start == -1)
            {
                status.postMessage(new StatusMessage(StatusType.WARNING, "Start time is invalid"));
            }
            else if(end == -1)
            {
                status.postMessage(new StatusMessage(StatusType.WARNING, "End time is invalid"));
            }
            else if(start >= end)
            {
                status.postMessage(new StatusMessage(StatusType.WARNING, "Start time must be after end time"));
            }
            else
            {
                playbackPanel.playSubsection(start, end);
            }
        });
        
        saveButton.addActionListener(e -> {
            status.postMessage(new StatusMessage(StatusType.INFO, "Saving clip: " + titleField.getText()));
            String extension = videoFile.getName().substring(videoFile.getName().indexOf('.'));
            clipQueue.enqueue(new ClipJob(videoFile.getName(), titleField.getText() + extension,
                                          startTimeField.getText(), endTimeField.getText()));
        });
    }
    
    public void setRecording(File videoFile)
    {
        this.videoFile = videoFile;
        playbackPanel.play(videoFile);
    }
}
