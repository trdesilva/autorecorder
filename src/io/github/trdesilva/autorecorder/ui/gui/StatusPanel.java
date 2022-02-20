/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.status.StatusConsumer;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.IOUtils;

import javax.inject.Named;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StatusPanel extends DefaultPanel implements StatusConsumer
{
    private final StatusQueue status;
    private final boolean isDebugMode;
    
    private final JLabel messageLabel;
    private ImageIcon recordingOnIcon;
    private ImageIcon recordingOffIcon;
    private JLabel recordingIndicator;
    private MouseListener mouseListener;
    
    @Inject
    public StatusPanel(StatusQueue status, @Named("isDebugMode") boolean isDebugMode)
    {
        this.status = status;
        this.isDebugMode = isDebugMode;
        
        setLayout(new MigLayout("fill", "[grow][16!]"));
        
        messageLabel = new JLabel("Welcome to Autorecorder");
        try
        {
            recordingOnIcon = new ImageIcon(
                    IOUtils.resourceToByteArray("recordingonicon.png", getClass().getClassLoader()));
            recordingOnIcon.setImage(recordingOnIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            recordingOffIcon = new ImageIcon(
                    IOUtils.resourceToByteArray("recordingofficon.png", getClass().getClassLoader()));
            recordingOffIcon.setImage(recordingOffIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            recordingIndicator = new JLabel(recordingOffIcon);
        }
        catch(IOException e)
        {
            status.postMessage(new StatusMessage(StatusType.DEBUG, "Failed to load icons"));
            recordingOnIcon = null;
            recordingOffIcon = null;
            recordingIndicator = new JLabel("Stopped");
        }
        setRecordingIndicatorState(false);
        
        add(messageLabel, "cell 0 0, growx");
        add(recordingIndicator, "cell 1 0");
    }
    
    @Override
    public synchronized void post(StatusMessage message) throws InterruptedException
    {
        System.out.println(message);
        boolean pause = true;
        boolean showMessage = true;
        if(message.getType() == StatusType.DEBUG)
        {
            pause = false;
            showMessage = isDebugMode;
        }
        
        if(showMessage)
        {
            switch(message.getType())
            {
                case FAILURE:
                    setBackground(Color.RED);
                    break;
                case SUCCESS:
                    setBackground(Color.GREEN);
                    break;
                case WARNING:
                    setBackground(Color.YELLOW);
                    break;
                case INFO:
                    setBackground(Color.CYAN);
                    break;
                case RECORDING_START:
                    setRecordingIndicatorState(true);
                    break;
                case RECORDING_END:
                    setRecordingIndicatorState(false);
                    break;
                default:
                    setBackground(Color.LIGHT_GRAY);
            }
            
            messageLabel.setText(message.getMessage());
            messageLabel.setToolTipText(message.getTimestamp().toString());
            
            String link = message.getLink();
            if(link != null)
            {
                mouseListener = new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        try
                        {
                            Desktop.getDesktop().browse(new URI(link));
                        }
                        catch(URISyntaxException | IOException ex)
                        {
                            status.postMessage(new StatusMessage(StatusType.DEBUG, "Link navigation failed: " + link));
                        }
                    }
                };
                messageLabel.addMouseListener(mouseListener);
                messageLabel.setText(
                        String.format("<html>%s (<a href='%s'>%s</a>)</html>", message.getMessage(), link, link));
                messageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            else
            {
                messageLabel.removeMouseListener(mouseListener);
                messageLabel.setCursor(Cursor.getDefaultCursor());
            }
        }
        
        // TODO if there are ever any other status consumers, this won't work
        if(pause)
        {
            Thread.sleep(1000);
        }
    }
    
    private void setRecordingIndicatorState(boolean isRecording)
    {
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
            recordingIndicator.setToolTipText("Recording");
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
            recordingIndicator.setToolTipText("Not recording");
        }
    }
}
