/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.VideoPlaybackPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
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
import javax.swing.UIManager;
import java.io.File;

public class UploadPanel extends DefaultPanel
{
    private final VideoPlaybackPanel playbackPanel;
    private final JTextField titleField;
    private File videoFile;
    
    @Inject
    public UploadPanel(VideoPlaybackPanel playbackPanel, UploadQueue uploadQueue, StatusQueue status,
                       Navigator navigator)
    {
        setLayout(new MigLayout("fill", "[grow]", "[30!][grow][]"));
        
        JButton backButton = new JButton("Back");
        
        this.playbackPanel = playbackPanel;
        
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
        uploadButton.setToolTipText(
                "By clicking 'Upload,' you certify that the content you are uploading complies with the YouTube Terms of Service (including the YouTube Community Guidelines) at https://www.youtube.com/t/terms. Please be sure not to violate others' copyright or privacy rights.");
        
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
            
            navigator.showMainView();
        });
        
        uploadButton.addActionListener(e -> {
            UploadJob job = new UploadJob(videoFile.getAbsolutePath(), titleField.getText(),
                                          descriptionField.getText());
            job.addProperty(YoutubeUploader.PRIVACY_PROPERTY,
                            ((PrivacyStatus) (privacySelector.getSelectedItem())).name());
            status.postMessage(
                    new StatusMessage(StatusType.INFO, "Adding " + job.getVideoTitle() + " to upload queue"));
            uploadQueue.enqueue(job);
        });
    }
    
    public void setRecording(File videoFile)
    {
        this.videoFile = videoFile;
        playbackPanel.play(videoFile);
        titleField.setText(videoFile.getName().substring(0, videoFile.getName().indexOf('.')));
    }
}
