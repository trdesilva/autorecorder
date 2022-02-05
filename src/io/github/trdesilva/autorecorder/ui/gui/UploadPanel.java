package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadQueue;
import io.github.trdesilva.autorecorder.upload.youtube.PrivacyStatus;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import java.io.File;

public class UploadPanel extends JPanel
{
    private VideoPlaybackPanel playbackPanel;
    private final JTextField titleField;
    private File videoFile;
    
    public UploadPanel(UploadQueue uploadQueue)
    {
        setLayout(new MigLayout("fill", "[grow]", "[30!][grow][]"));
    
        JButton backButton = new JButton("Back");
    
        playbackPanel = new VideoPlaybackPanel();
    
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new MigLayout("fill", "[left, 160::30%][80:30%:90%:push][shrink][right, shrink]", "[]"));
    
        JLabel titleLabel = new JLabel("Title");
        titleField = new JTextField();
        titleField.setColumns(25);
        JLabel descriptionLabel = new JLabel("Description");
        JScrollPane descriptionScrollPane = new JScrollPane();
        JTextArea descriptionField = new JTextArea();
        descriptionField.setLineWrap(true);
        descriptionField.setFont(UIManager.getFont("Label.font"));
        descriptionScrollPane.getViewport().add(descriptionField);
        JComboBox<PrivacyStatus> privacySelector = new JComboBox<>(PrivacyStatus.values());
        JButton uploadButton = new JButton("Upload to YouTube");
    
        controlPanel.add(titleLabel, "cell 0 0");
        controlPanel.add(titleField, "cell 0 0, grow");
        controlPanel.add(descriptionLabel, "cell 1 0");
        controlPanel.add(descriptionScrollPane, "cell 1 0, grow");
        controlPanel.add(privacySelector, "cell 2 0");
        controlPanel.add(uploadButton, "cell 3 0");
    
        add(backButton, "cell 0 0");
        add(playbackPanel, "cell 0 1, grow, wmin 400, hmin 300");
        add(controlPanel, "cell 0 2, growx");
    
        backButton.addActionListener(e -> {
            playbackPanel.stop();
        
            titleField.setText("");
            descriptionField.setText("");
        
            MainWindow.getInstance().showMainView();
        });
        
        uploadButton.addActionListener(e -> {
            UploadJob job = new UploadJob(videoFile.getAbsolutePath(), titleField.getText(), descriptionField.getText());
            job.addProperty(YoutubeUploader.PRIVACY_PROPERTY, ((PrivacyStatus)(privacySelector.getSelectedItem())).name());
            uploadQueue.enqueue(job);
            StatusQueue.postMessage(new StatusMessage(StatusType.INFO, "Adding " + job.getVideoTitle() + " to upload queue"));
        });
    }
    
    public void setRecording(File videoFile)
    {
        this.videoFile = videoFile;
        playbackPanel.play(videoFile);
        titleField.setText(videoFile.getName().substring(0, videoFile.getName().indexOf('.')));
    }
}
