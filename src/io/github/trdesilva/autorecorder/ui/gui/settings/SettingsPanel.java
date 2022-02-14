/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.settings;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.SettingsValidator;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SettingsPanel extends DefaultPanel
{
    @Inject
    public SettingsPanel(Settings settings, StatusQueue status, SettingsValidator validator, Navigator navigator)
    {
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
        JButton licenseButton = new JButton("View License/Terms of Use");
        JButton saveButton = new JButton("Save");
        
        add(obsPathLabel, "cell 0 0");
        add(obsPathField, "cell 0 0, growx");
        add(recordingPathLabel, "cell 0 1");
        add(recordingPathField, "cell 0 1, growx");
        add(clipPathLabel, "cell 1 1");
        add(clipPathField, "cell 1 1, growx");
        add(additionalGamesPanel, "cell 0 2, grow");
        add(excludedGamesPanel, "cell 1 2, grow");
        add(licenseButton, "cell 0 3, left");
        add(saveButton, "cell 1 3, right, tag apply");
        
        licenseButton.addActionListener(e -> {
            navigator.showLicenseView();
        });
        
        saveButton.addActionListener(e -> {
            Settings.SettingsContainer tempSettings = new Settings.SettingsContainer();
            tempSettings.obsPath = obsPathField.getText();
            tempSettings.recordingPath = recordingPathField.getText();
            tempSettings.clipPath = clipPathField.getText();
            tempSettings.additionalGames = additionalGamesPanel.getGames();
            tempSettings.excludedGames = excludedGamesPanel.getGames();
            
            if(validator.validate(tempSettings))
            {
                settings.setObsPath(obsPathField.getText());
                settings.setRecordingPath(recordingPathField.getText());
                settings.setClipPath(clipPathField.getText());
                settings.setAdditionalGames(additionalGamesPanel.getGames());
                settings.setExcludedGames(excludedGamesPanel.getGames());
                
                settings.save();
                status.postMessage(new StatusMessage(StatusType.SUCCESS, "Settings saved"));
            }
        });
    }
}
