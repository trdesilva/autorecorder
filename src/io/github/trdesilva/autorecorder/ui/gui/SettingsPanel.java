/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.SettingsValidator;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SettingsPanel extends JPanel
{
    private Settings settings;
    
    public SettingsPanel(Settings settings)
    {
        this.settings = settings;
        
        setLayout(new MigLayout("fill", "[50%][50%]", "[][][grow]push[]"));
    
        JLabel obsPathLabel = new JLabel("OBS Path");
        JTextField obsPathField = new JTextField(settings.getObsPath());
        JLabel recordingPathLabel = new JLabel("Recording Path");
        JTextField recordingPathField = new JTextField(settings.getRecordingPath());
        JLabel clipPathLabel = new JLabel("Clip Path");
        JTextField clipPathField = new JTextField(settings.getClipPath());
        GameListPanel additionalGamesPanel = new GameListPanel(settings.getAdditionalGames(), "Additional Games",
                                                               "These executables will be added to the auto-detection list");
        GameListPanel excludedGamesPanel = new GameListPanel(settings.getExcludedGames(), "Excluded Games",
                                                             "These executables will be removed from the auto-detection list (they will not trigger recording)");
        JButton saveButton = new JButton("Save");
        
        add(obsPathLabel, "cell 0 0");
        add(obsPathField, "cell 0 0, growx");
        add(recordingPathLabel, "cell 0 1");
        add(recordingPathField, "cell 0 1, growx");
        add(clipPathLabel, "cell 1 1");
        add(clipPathField, "cell 1 1, growx");
        add(additionalGamesPanel, "cell 0 2, grow");
        add(excludedGamesPanel, "cell 1 2, grow");
        add(saveButton, "cell 1 3, right");
        
        saveButton.addActionListener(e -> {
            Settings tempSettings = new Settings();
            tempSettings.setObsPath(obsPathField.getText());
            tempSettings.setRecordingPath(recordingPathField.getText());
            tempSettings.setClipPath(clipPathField.getText());
            tempSettings.setAdditionalGames(additionalGamesPanel.getGames());
            tempSettings.setExcludedGames(excludedGamesPanel.getGames());
            
            if(SettingsValidator.validate(tempSettings))
            {
                settings.setObsPath(obsPathField.getText());
                settings.setRecordingPath(recordingPathField.getText());
                settings.setClipPath(clipPathField.getText());
                settings.setAdditionalGames(additionalGamesPanel.getGames());
                settings.setExcludedGames(excludedGamesPanel.getGames());
    
                settings.save();
                StatusQueue.postMessage(new StatusMessage(StatusType.SUCCESS, "Settings saved"));
            }
        });
    }
}
