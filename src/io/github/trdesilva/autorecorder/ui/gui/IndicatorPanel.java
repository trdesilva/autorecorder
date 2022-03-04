/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.IOUtils;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Set;

public class IndicatorPanel extends JPanel implements EventConsumer
{
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.RECORDING_START,
                                                                            EventType.RECORDING_END,
                                                                            EventType.CLIP_START,
                                                                            EventType.CLIP_END,
                                                                            EventType.UPLOAD_START,
                                                                            EventType.UPLOAD_END);
    
    private ImageIcon recordingOnIcon;
    private ImageIcon recordingOffIcon;
    private ImageIcon uploadIcon;
    private ImageIcon clipIcon;
    
    private JLabel recordingIndicator;
    private JLabel uploadIndicator;
    private JLabel clipIndicator;
    
    @Inject
    public IndicatorPanel(EventQueue events)
    {
        setLayout(new MigLayout("filly, aligny center", "[16!][16!][16!]", "[16!]"));
        try
        {
            recordingOnIcon = new ImageIcon(
                    IOUtils.resourceToByteArray("recordingonicon.png", getClass().getClassLoader()));
            recordingOnIcon.setImage(recordingOnIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            recordingOffIcon = new ImageIcon(
                    IOUtils.resourceToByteArray("recordingofficon.png", getClass().getClassLoader()));
            recordingOffIcon.setImage(recordingOffIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            
            uploadIcon = new ImageIcon(IOUtils.resourceToByteArray("uploadicon.png", getClass().getClassLoader()));
            uploadIcon.setImage(uploadIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            
            clipIcon = new ImageIcon(IOUtils.resourceToByteArray("clipicon.png", getClass().getClassLoader()));
            clipIcon.setImage(clipIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            
            recordingIndicator = new JLabel(recordingOffIcon);
            recordingIndicator.setToolTipText("Not recording, click to force start");
            uploadIndicator = new JLabel(uploadIcon);
            uploadIndicator.setVisible(false);
            clipIndicator = new JLabel(clipIcon);
            clipIndicator.setVisible(false);
        }
        catch(IOException e)
        {
            events.postEvent(new Event(EventType.DEBUG, "Failed to load icons: " + e.toString()));
            recordingOnIcon = null;
            recordingOffIcon = null;
            uploadIcon = null;
            clipIcon = null;
            
            recordingIndicator = new JLabel("S");
            uploadIndicator = new JLabel("U");
            uploadIndicator.setVisible(false);
            clipIndicator = new JLabel("C");
            clipIndicator.setVisible(false);
        }
        
        recordingIndicator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(recordingOnIcon != null && recordingOffIcon != null)
                {
                    if(recordingIndicator.getIcon().equals(recordingOnIcon))
                    {
                        events.postEvent(new Event(EventType.MANUAL_RECORDING_END, "Requesting manual recording stop"));
                    }
                    else
                    {
                        events.postEvent(new Event(EventType.MANUAL_RECORDING_START, "Requesting manual recording start"));
                    }
                }
                else
                {
                    if(recordingIndicator.getText().equals("R"))
                    {
                        events.postEvent(new Event(EventType.MANUAL_RECORDING_END, "Requesting manual recording stop"));
                    }
                    else if(recordingIndicator.getText().equals("S"))
                    {
                        events.postEvent(new Event(EventType.MANUAL_RECORDING_START, "Requesting manual recording start"));
                    }
                }
            }
        });
        
        add(clipIndicator, "center, growy");
        add(uploadIndicator, "center, growy");
        add(recordingIndicator, "center, growy");
        
        events.addConsumer(this);
    }
    
    @Override
    public void post(Event event)
    {
        switch(event.getType())
        {
            case RECORDING_START:
            case RECORDING_END:
                setRecordingIndicatorState(event);
                break;
            case CLIP_START:
            case CLIP_END:
                setClipIndicatorState(event);
                break;
            case UPLOAD_START:
            case UPLOAD_END:
                setUploadIndicatorState(event);
                break;
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
    
    private void setRecordingIndicatorState(Event event)
    {
        boolean isRecording = event.getType() == EventType.RECORDING_START;
        if(isRecording)
        {
            if(recordingOnIcon != null)
            {
                recordingIndicator.setIcon(recordingOnIcon);
            }
            else
            {
                recordingIndicator.setText("R");
            }
            recordingIndicator.setToolTipText(
                    String.format("%s (started at %s, click to force stop)", event.getMessage(), event.getTimestamp().toLocalTime()));
        }
        else
        {
            if(recordingOffIcon != null)
            {
                recordingIndicator.setIcon(recordingOffIcon);
            }
            else
            {
                recordingIndicator.setText("S");
            }
            recordingIndicator.setToolTipText("Not recording, click to force start");
        }
    }
    
    private void setUploadIndicatorState(Event event)
    {
        boolean isUploading = event.getType() == EventType.UPLOAD_START;
        if(isUploading)
        {
            uploadIndicator.setVisible(true);
            uploadIndicator.setToolTipText(
                    String.format("%s (started at %s)", event.getMessage(), event.getTimestamp().toLocalTime()));
        }
        else
        {
            uploadIndicator.setVisible(false);
            uploadIndicator.setToolTipText("");
        }
    }
    
    private void setClipIndicatorState(Event event)
    {
        boolean isClipping = event.getType() == EventType.CLIP_START;
        if(isClipping)
        {
            clipIndicator.setVisible(true);
            clipIndicator.setToolTipText(
                    String.format("%s (started at %s)", event.getMessage(), event.getTimestamp().toLocalTime()));
        }
        else
        {
            clipIndicator.setVisible(false);
            clipIndicator.setToolTipText("");
        }
    }
}
