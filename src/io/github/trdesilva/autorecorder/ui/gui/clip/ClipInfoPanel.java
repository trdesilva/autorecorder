/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoInfoPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;
import io.github.trdesilva.autorecorder.video.VideoMetadata;
import io.github.trdesilva.autorecorder.video.VideoMetadataHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;

public class ClipInfoPanel extends VideoInfoPanel
{
    private final VideoMetadataHandler metadataHandler;
    
    private final WrappingLabel title;
    private final JLabel duration;
    private final JLabel resolution;
    private final WrappingLabel uploadLink;
    
    private final JButton uploadButton;
    
    private MouseListener mouseListener;
    
    @Inject
    public ClipInfoPanel(VideoMetadataHandler metadataHandler, EventQueue eventQueue, Navigator navigator)
    {
        super(metadataHandler, eventQueue);
        this.metadataHandler = metadataHandler;
        
        setLayout(new MigLayout("fill", "[100:null:null]", "[][][][][]push[]"));
        
        title = new WrappingLabel("");
        title.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 16));
        duration = new JLabel("");
        resolution = new JLabel("");
        uploadLink = new WrappingLabel("");
        
        uploadButton = new JButton("Upload...");
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(e -> {
            if(currentVideo != null)
            {
                navigator.showUploadView(currentVideo);
            }
        });
        
        add(title, "cell 0 0, growx, wmin 100");
        add(duration, "cell 0 1, growx, wmin 100");
        add(resolution, "cell 0 2, growx, wmin 100");
        add(uploadLink, "cell 0 3, growx, wmin 100");
        add(thumbnailLabel, "cell 0 4, growx, ax 0.5al, wmin 100");
        add(uploadButton, "cell 0 5, growx, wmin 100, tag next");
    }
    
    @Override
    public void updateUi(File video)
    {
        if(video != null)
        {
            VideoMetadata metadata = metadataHandler.getMetadata(video);
            title.setText(video.getName());
            duration.setText("Duration: " + formatTime(metadata.getDuration()));
            resolution.setText("Resolution: " + metadata.getResolution());
            String uploadUrl = metadata.getUploadLink();
            if(!uploadUrl.isBlank())
            {
                uploadLink.setText(String.format("Upload Link: %s", uploadUrl));
                
                uploadLink.removeMouseListener(mouseListener);
                thumbnailLabel.removeMouseListener(mouseListener);
                mouseListener = new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        try
                        {
                            Desktop.getDesktop().browse(new URI(uploadUrl));
                        }
                        catch(URISyntaxException | IOException ex)
                        {
                            eventQueue.postEvent(new Event(EventType.DEBUG, "Link navigation failed: " + uploadUrl));
                        }
                    }
                };
                uploadLink.addMouseListener(mouseListener);
                thumbnailLabel.addMouseListener(mouseListener);
                uploadLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                thumbnailLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            else
            {
                uploadLink.setText("");
                uploadLink.removeMouseListener(mouseListener);
                thumbnailLabel.removeMouseListener(mouseListener);
                uploadLink.setCursor(Cursor.getDefaultCursor());
                thumbnailLabel.setCursor(Cursor.getDefaultCursor());
            }
            loadThumbnail(video);
            uploadButton.setEnabled(true);
        }
        else
        {
            title.setText("");
            duration.setText("");
            resolution.setText("");
            uploadLink.setText("");
            uploadLink.removeMouseListener(mouseListener);
            uploadLink.setCursor(Cursor.getDefaultCursor());
            thumbnailLabel.setIcon(null);
            thumbnailLabel.setText("");
            thumbnailLabel.removeMouseListener(mouseListener);
            thumbnailLabel.setCursor(Cursor.getDefaultCursor());
            uploadButton.setEnabled(false);
        }
    }
}
