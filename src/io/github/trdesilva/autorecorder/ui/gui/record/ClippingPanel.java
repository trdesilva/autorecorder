/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.clip.ClipJob;
import io.github.trdesilva.autorecorder.clip.ClipQueue;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoPlaybackPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.io.File;

import static io.github.trdesilva.autorecorder.TimestampUtil.parseTime;

public class ClippingPanel extends DefaultPanel
{
    private final VideoPlaybackPanel playbackPanel;
    private final VideoListHandler recordingHandler;
    
    private File videoFile;
    private final JList<Long> bookmarkList;
    
    @Inject
    public ClippingPanel(VideoPlaybackPanel playbackPanel, ClipQueue clipQueue, EventQueue events, Navigator navigator,
                         @Named("RECORDING") VideoListHandler recordingHandler)
    {
        setLayout(new MigLayout("fill, hidemode 2", "[grow][::200]", "[30!][grow][]"));
        
        JButton backButton = new JButton("Back");
        JButton expandButton = new JButton("<");
        
        this.playbackPanel = playbackPanel;
        this.recordingHandler = recordingHandler;
        
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
        
        JPanel advancedPanel = new JPanel();
        advancedPanel.setLayout(new MigLayout("fill, hidemode 2", "[80::200]", "[][grow]"));
        advancedPanel.setVisible(false);
        JLabel bookmarkLabel = new JLabel("Bookmarks");
        JScrollPane bookmarkPanel = new JScrollPane();
        bookmarkList = new JList<>();
        bookmarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookmarkList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            // TODO cursors don't seem to work inside of renderers
            JButton button = new JButton(TimestampUtil.formatTime(value));
            return button;
        });
        bookmarkPanel.getViewport().add(bookmarkList);
        
        advancedPanel.add(bookmarkLabel, "cell 0 0");
        advancedPanel.add(bookmarkPanel, "cell 0 1, wmin 70, hmin 80");
        
        add(backButton, "cell 0 0, split 2, left");
        add(expandButton, "cell 0 0, gapleft push");
        add(advancedPanel, "cell 1 0, span 1 3, top, wmin 80");
        add(playbackPanel, "cell 0 1, grow, wmin 400, hmin 300");
        add(controlPanel, "cell 0 2, growx");
        
        backButton.addActionListener(e -> {
            playbackPanel.stop();
            
            titleField.setText("");
            startTimeField.setText("");
            endTimeField.setText("");
            
            navigator.showMainView();
        });
        
        expandButton.addActionListener(e -> {
            boolean showAdvancedPanel = !advancedPanel.isVisible();
            advancedPanel.setVisible(showAdvancedPanel);
            expandButton.setText(showAdvancedPanel ? ">" : "<");
            revalidate();
        });
        
        bookmarkList.addListSelectionListener(e -> {
            // need to check these things for clearing the selection to work right
            if(0 <= e.getFirstIndex() && e.getLastIndex() < bookmarkList.getModel()
                                                                        .getSize() && !e.getValueIsAdjusting())
            {
                playbackPanel.seekTo(bookmarkList.getModel().getElementAt(e.getFirstIndex()), true);
                SwingUtilities.invokeLater(bookmarkList::clearSelection);
            }
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
                events.postEvent(new Event(EventType.WARNING, "Start time is invalid"));
            }
            else if(end == -1)
            {
                events.postEvent(new Event(EventType.WARNING, "End time is invalid"));
            }
            else if(start >= end)
            {
                events.postEvent(new Event(EventType.WARNING, "Start time must be after end time"));
            }
            else
            {
                playbackPanel.playSubsection(start, end);
            }
        });
        
        saveButton.addActionListener(e -> {
            events.postEvent(new Event(EventType.INFO, "Adding clip to queue: " + titleField.getText()));
            String extension = videoFile.getName().substring(videoFile.getName().indexOf('.'));
            clipQueue.enqueue(new ClipJob(videoFile.getName(), titleField.getText() + extension,
                                          startTimeField.getText(), endTimeField.getText()));
        });
    }
    
    public void setRecording(File videoFile)
    {
        this.videoFile = videoFile;
        playbackPanel.play(videoFile);
        bookmarkList.setListData(recordingHandler.getMetadata(videoFile)
                                                 .getBookmarks().toArray(new Long[0]));
    }
}
