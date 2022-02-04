package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.clip.ClipJob;
import io.github.trdesilva.autorecorder.clip.ClipQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.File;

public class ClippingPanel extends JPanel
{
    private VideoPlaybackPanel playbackPanel;
    private File videoFile;
    
    public ClippingPanel(ClipQueue clipQueue)
    {
        setLayout(new MigLayout("fill", "[grow]", "[30!][grow][]"));
        
        JButton backButton = new JButton("Back");
        
        playbackPanel = new VideoPlaybackPanel();
        
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
            
            MainWindow.getInstance().showMainView();
        });
    
        startCurrentTimeButton.addActionListener(e -> {
            startTimeField.setText(TimestampUtil.formatTime(playbackPanel.getPlaybackTime()));
        });
    
        endCurrentTimeButton.addActionListener(e -> {
            endTimeField.setText(TimestampUtil.formatTime(playbackPanel.getPlaybackTime()));
        });
        
        previewButton.addActionListener(e -> {
        
        });
        
        saveButton.addActionListener(e -> {
            // TODO input validation
            String extension = videoFile.getName().substring(videoFile.getName().indexOf('.'));
            clipQueue.enqueue(new ClipJob(videoFile.getAbsolutePath(), titleField.getText() + extension,
                                          startTimeField.getText(), endTimeField.getText()));
            StatusQueue.getInstance().postMessage(new StatusMessage(StatusType.INFO, "Saving clip: " + titleField.getText()));
        });
    }
    
    public void setRecording(File videoFile)
    {
        this.videoFile = videoFile;
        playbackPanel.play(videoFile);
    }
}
