/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.record;

import com.google.common.collect.Lists;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.trdesilva.autorecorder.TimestampUtil.parseTime;

public class ClippingPanel extends DefaultPanel
{
    private final VideoPlaybackPanel playbackPanel;
    private final VideoListHandler recordingHandler;
    private final JTextField startTimeField;
    private final JTextField endTimeField;
    private final EventQueue events;
    private final SegmentListPanel segmentListPanel;
    
    private File videoFile;
    private final JList<Long> bookmarkList;
    
    @Inject
    public ClippingPanel(VideoPlaybackPanel playbackPanel, ClipQueue clipQueue, EventQueue events, Navigator navigator,
                         @Named("RECORDING") VideoListHandler recordingHandler)
    {
        setLayout(new MigLayout("fill, hidemode 2", "[grow][::200]", "[30!][grow][]"));
        
        JButton backButton = new JButton("Back");
        JButton expandButton = new JButton("⭰");
        
        this.playbackPanel = playbackPanel;
        this.recordingHandler = recordingHandler;
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new MigLayout("fill", "[][grow][][][80!][][][80!][][]", "[]"));
        
        JLabel titleLabel = new JLabel("Title");
        JLabel startTimeLabel = new JLabel("Start");
        JLabel endTimeLabel = new JLabel("End");
        
        JTextField titleField = new JTextField();
        startTimeField = new JTextField();
        startTimeField.setColumns(10);
        endTimeField = new JTextField();
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
        advancedPanel.setLayout(new MigLayout("fill, hidemode 2", "[160::200]", "[][grow][][grow]"));
        advancedPanel.setVisible(false);
        advancedPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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
        JLabel segmentLabel = new JLabel("Segments");
        segmentListPanel = new SegmentListPanel();
        
        advancedPanel.add(bookmarkLabel, "cell 0 0");
        advancedPanel.add(bookmarkPanel, "cell 0 1, wmin 140, wmax 150, hmin 80");
        advancedPanel.add(segmentLabel, "cell 0 2");
        advancedPanel.add(segmentListPanel, "cell 0 3, wmin 140, wmax 150, hmin 80");
        
        add(backButton, "cell 0 0, split 2, left");
        add(expandButton, "cell 0 0, gapleft push");
        add(advancedPanel, "cell 1 0, span 1 3, top, wmin 160");
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
            expandButton.setText(showAdvancedPanel ? "⭲" : "⭰");
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
        
        startTimeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e)
            {
                handleTimeInput(e, startTimeField);
            }
        });
        
        startCurrentTimeButton.addActionListener(e -> {
            startTimeField.setText(TimestampUtil.formatTime(playbackPanel.getPlaybackTime(), true));
        });
    
        endTimeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e)
            {
                handleTimeInput(e, endTimeField);
            }
        });
        
        endCurrentTimeButton.addActionListener(e -> {
            endTimeField.setText(TimestampUtil.formatTime(playbackPanel.getPlaybackTime(), true));
        });
    
        this.events = events;
        previewButton.addActionListener(e -> {
            List<Segment> segments = segmentListPanel.getSegments();
            if(segments.isEmpty())
            {
                playPreview(startTimeField.getText(), endTimeField.getText());
            }
            else
            {
                List<Long> starts = segments.parallelStream().map(Segment::getStart).map(TimestampUtil::parseTime).collect(Collectors.toList());
                List<Long> ends = segments.parallelStream().map(Segment::getEnd).map(TimestampUtil::parseTime).collect(Collectors.toList());
                // starts and ends were validated when the segments were added, so don't need to validate again
                playbackPanel.playSubsections(starts, ends);
            }
        });
        
        saveButton.addActionListener(e -> {
            this.events.postEvent(new Event(EventType.INFO, "Adding clip to queue: " + titleField.getText()));
            String extension = videoFile.getName().substring(videoFile.getName().indexOf('.'));
            List<Segment> segments = segmentListPanel.getSegments();
            ClipJob clipJob;
            if(segments.isEmpty())
            {
                clipJob = new ClipJob(videoFile.getName(), titleField.getText() + extension,
                                      startTimeField.getText(), endTimeField.getText());
            }
            else
            {
                List<String> starts = segments.parallelStream().map(Segment::getStart).collect(Collectors.toList());
                List<String> ends = segments.parallelStream().map(Segment::getEnd).collect(Collectors.toList());
                clipJob = new ClipJob(videoFile.getName(), titleField.getText() + extension, starts, ends);
            }
            clipQueue.enqueue(clipJob);
        });
    }
    
    private static void handleTimeInput(KeyEvent e, JTextField timeField)
    {
        char nextChar = e.getKeyChar();
        if(!(('0' <= nextChar && nextChar <= '9') || nextChar == ':' || nextChar == '.' || nextChar == '\b'))
        {
            e.consume();
            return;
        }
        
        String newValue = timeField.getText();
        if(nextChar != '\b')
        {
            // if the caret is at the end and nothing is selected, append to end
            timeField.replaceSelection(Character.toString(nextChar));
            newValue = timeField.getText();
            int colonCount = 0;
            int firstColonIndex = newValue.indexOf(':');; // allows for non-2-digit hours
            if(firstColonIndex != -1)
            {
                colonCount = 1;
                firstColonIndex = newValue.indexOf(':');
                if(newValue.lastIndexOf(':') != firstColonIndex)
                {
                    colonCount = 2;
                }
            }
            else
            {
                // if there isn't a colon yet, assume 2-digit hour
                firstColonIndex = 2;
            }
            
            if(colonCount == 0)
            {
                if(newValue.length() >= firstColonIndex)
                {
                    // inserting at the index makes replacing a multiple-character selection work
                    // (e.g. "01|.3|" + '2' -> "01.2" instead of "012")
                    newValue = insertAt(newValue, firstColonIndex, ':');
                    timeField.setText(newValue);
                }
            }
            else if(colonCount == 1)
            {
                if(newValue.length() >= firstColonIndex + 3)
                {
                    newValue = insertAt(newValue, firstColonIndex + 3, ':');
                    timeField.setText(newValue);
                }
            }
            else if(newValue.indexOf('.') == -1)
            {
                if(newValue.length() >= firstColonIndex + 6)
                {
                    newValue = insertAt(newValue, firstColonIndex + 6, '.');
                    timeField.setText(newValue);
                }
            }
            e.consume();
        }
        else if(newValue.length() > 0)
        {
            // for some reason, backspace is applied before keyTyped() while normal characters aren't, so no need
            // to add it to newValue here like above
            if(newValue.charAt(newValue.length() - 1) == ':' || newValue.charAt(newValue.length() - 1) == '.')
            {
                newValue = newValue.substring(0, newValue.length() - 1);
                timeField.setText(newValue);
                e.consume();
            }
        }
    }
    
    private static String insertAt(String str, int index, char c)
    {
        if(str.length() == index)
        {
            str = str.substring(0, index) + c;
        }
        else
        {
            str = str.substring(0, index) + c + str.substring(index);
        }
        return str;
    }
    
    private void playPreview(String startTimeText, String endTimeText)
    {
        long start = parseTime(startTimeText);
        long end = parseTime(endTimeText);
        if(start == -1)
        {
            this.events.postEvent(new Event(EventType.WARNING, "Start time is invalid"));
        }
        else if(end == -1)
        {
            this.events.postEvent(new Event(EventType.WARNING, "End time is invalid"));
        }
        else if(start >= end)
        {
            this.events.postEvent(new Event(EventType.WARNING, "Start time must be before end time"));
        }
        else
        {
            playbackPanel.playSubsection(start, end);
        }
    }
    
    public void setRecording(File videoFile)
    {
        this.videoFile = videoFile;
        playbackPanel.play(videoFile);
        bookmarkList.setListData(recordingHandler.getMetadata(videoFile)
                                                 .getBookmarks().toArray(new Long[0]));
        segmentListPanel.reset();
    }
    
    private class SegmentListPanel extends JPanel
    {
        private final JList<Segment> segmentList;
        private final DefaultListModel<Segment> segmentListModel;
    
        public SegmentListPanel()
        {
            setLayout(new MigLayout("fill", "0[grow][40!]", "[][][][]"));
            
            JScrollPane scrollPane = new JScrollPane();
            segmentListModel = new DefaultListModel<>();
            segmentList = new JList<>(segmentListModel);
            scrollPane.getViewport().add(segmentList);
            
            JButton addButton = new JButton("+");
            addButton.setToolTipText("Add a segment with the currently specified Start and End times");
            JButton removeButton = new JButton("-");
            removeButton.setToolTipText("Remove the selected segment");
            removeButton.setEnabled(false);
            JButton moveUpButton = new JButton("↑"); // cursed UTF-8
            moveUpButton.setToolTipText("Switch the selected segment with the previous one");
            moveUpButton.setEnabled(false);
            JButton moveDownButton = new JButton("↓");
            moveDownButton.setToolTipText("Switch the selected segment with the next one");
            moveDownButton.setEnabled(false);
            
            add(scrollPane, "cell 0 0, span 1 4, grow");
            add(addButton, "cell 1 0, wmin 40");
            add(removeButton, "cell 1 1, wmin 40");
            add(moveUpButton, "cell 1 2, wmin 40");
            add(moveDownButton, "cell 1 3, wmin 40, top");
            
            addButton.addActionListener(e -> {
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
                    events.postEvent(new Event(EventType.WARNING, "Start time must be before end time"));
                }
                else
                {
                    segmentListModel.addElement(new Segment(startTimeField.getText(), endTimeField.getText()));
                }
            });
            
            segmentList.addListSelectionListener(e -> {
                int selection = segmentList.getSelectedIndex();
                moveUpButton.setEnabled(selection > 0);
                moveDownButton.setEnabled(selection != -1 && selection < segmentListModel.getSize() - 1);
                removeButton.setEnabled(selection > -1);
                Segment segment = segmentList.getSelectedValue();
                if(segment != null)
                {
                    playPreview(segment.getStart(), segment.getEnd());
                    startTimeField.setText(segment.getStart());
                    endTimeField.setText(segment.getEnd());
                }
            });
            
            removeButton.addActionListener(e -> {
                int selection = segmentList.getSelectedIndex();
                if(selection > -1)
                {
                    segmentListModel.remove(selection);
                }
            });
            
            moveUpButton.addActionListener(e -> {
                int selection = segmentList.getSelectedIndex();
                if(selection > 0)
                {
                    Segment swap = segmentListModel.get(selection - 1);
                    segmentListModel.set(selection - 1, segmentListModel.get(selection));
                    segmentListModel.set(selection, swap);
                    segmentList.setSelectedIndex(selection - 1);
                }
            });
    
            moveDownButton.addActionListener(e -> {
                int selection = segmentList.getSelectedIndex();
                if(selection > -1 && selection < segmentListModel.getSize() - 1)
                {
                    Segment swap = segmentListModel.get(selection + 1);
                    segmentListModel.set(selection + 1, segmentListModel.get(selection));
                    segmentListModel.set(selection, swap);
                    segmentList.setSelectedIndex(selection + 1);
                }
            });
        }
        
        private Segment getSelection()
        {
            return segmentList.getSelectedValue();
        }
        
        private List<Segment> getSegments()
        {
            return Lists.newArrayList(segmentListModel.elements().asIterator());
        }
        
        private void reset()
        {
            segmentListModel.clear();
        }
    }
    
    private class Segment
    {
        private final String start;
        private final String end;
    
        public Segment(String start, String end)
        {
            this.start = start;
            this.end = end;
        }
    
        public String getStart()
        {
            return start;
        }
    
        public String getEnd()
        {
            return end;
        }
        
        @Override
        public String toString()
        {
            return String.format("%s - %s", start, end);
        }
    }
}
