/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;

public class RecordingInfoPanel extends VideoInfoPanel
{
    private final WrappingLabel title;
    private final WrappingLabel gameName;
    private final WrappingLabel creationDate;
    private final JLabel duration;
    private final JLabel resolution;
    private final JLabel bookmarks;
    
    private final JButton clipButton;
    
    @Inject
    public RecordingInfoPanel(VideoMetadataHandler metadataHandler, EventQueue eventQueue, Navigator navigator)
    {
        super(metadataHandler, eventQueue);
        setLayout(new MigLayout("fill", "[100:null:300]", "[][][][][][][]push[]"));
        
        title = new WrappingLabel("");
        title.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 16));
        gameName = new WrappingLabel("");
        creationDate = new WrappingLabel("");
        duration = new JLabel();
        duration.setHorizontalAlignment(SwingConstants.LEFT);
        resolution = new JLabel();
        bookmarks = new JLabel();
        
        clipButton = new JButton("Clip...");
        clipButton.setEnabled(false);
        clipButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(currentVideo != null)
                {
                    navigator.showClipView(currentVideo);
                }
            }
        });
        
        add(title, "cell 0 0, growx, wmin 100");
        add(gameName, "cell 0 1, growx, wmin 100");
        add(creationDate, "cell 0 2, growx, wmin 100");
        add(duration, "cell 0 3, growx, wmin 100");
        add(resolution, "cell 0 4, growx, wmin 100");
        add(bookmarks, "cell 0 5, growx, wmin 100");
        add(thumbnailLabel, "cell 0 6, grow, wmin 100");
        add(clipButton, "cell 0 7, growx, wmin 100");
    }
    
    @Override
    public void updateUi(File video)
    {
        if(video != null)
        {
            VideoMetadata metadata = metadataHandler.getMetadata(video);
            title.setText(video.getName());
            String gameNameStr = metadata.getGameName();
            if(!gameNameStr.isBlank())
            {
                gameName.setText("Game: " + gameNameStr);
            }
            else
            {
                gameName.setText("");
            }
            creationDate.setText("Created: " + metadata.getCreationDate());
            long duration = metadata.getDuration();
            this.duration.setText("Duration: " + formatTime(duration));
            resolution.setText("Resolution: " + metadata.getResolution());
            bookmarks.setText("Bookmarks: " + metadata.getBookmarks().size());
            
            loadThumbnail(video);
            
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
            gameName.setText("");
            creationDate.setText("");
            duration.setText("");
            resolution.setText("");
            bookmarks.setText("");
            thumbnailLabel.setIcon(null);
            thumbnailLabel.setText("");
            clipButton.setEnabled(false);
        }
    }
}