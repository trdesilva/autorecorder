/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.settings;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.SettingsValidator;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.record.Obs;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.ValidatingTextField;
import io.github.trdesilva.autorecorder.video.Hotkey;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SettingsPanel extends DefaultPanel
{
    @Inject
    public SettingsPanel(Settings settings, EventQueue events, SettingsValidator validator, Navigator navigator, Obs obs)
    {
        setLayout(new MigLayout("fill", "[50%][50%]", "[][][grow][]push[]"));
        
        JLabel obsPathLabel = new JLabel("OBS Path");
        JTextField obsPathField = new JTextField(settings.getObsPath());
        JLabel obsProfileLabel = new JLabel("OBS Profile");
        JComboBox<String> obsProfileDropdown = new JComboBox<>();
        obsProfileDropdown.setEditable(false);
        obsProfileDropdown.setModel(new DefaultComboBoxModel<>(obs.readProfileNames().toArray(new String[0])));
        
        JLabel recordingPathLabel = new JLabel("Recording Path");
        JTextField recordingPathField = new JTextField(settings.getRecordingPath());
        JLabel clipPathLabel = new JLabel("Clip Path");
        JTextField clipPathField = new JTextField(settings.getClipPath());
        
        GameListPanel additionalGamesPanel = new GameListPanel(settings.getAdditionalGames(), "Additional Games",
                                                               "These executables will be added to the auto-detection list");
        GameListPanel excludedGamesPanel = new GameListPanel(settings.getExcludedGames(), "Excluded Games",
                                                             "These executables will be removed from the auto-detection list (they will not trigger recording)");
        
        JPanel autoDeletePanel = new JPanel();
        autoDeletePanel.setLayout(new MigLayout("fill", "[][30!][grow]"));
        JCheckBox autoDeleteCheckbox = new JCheckBox();
        autoDeleteCheckbox.setSelected(settings.isAutoDeleteEnabled());
        autoDeleteCheckbox.setText("Automatically delete old recordings when over");
        autoDeleteCheckbox.setToolTipText(
                "When enabled, recordings will be deleted in order of age (oldest first) when a new recording starts and the total space used is over the limit.");
        ValidatingTextField autoDeleteThresholdField =
                new ValidatingTextField(Integer.toString(settings.getAutoDeleteThresholdGB()), input -> {
                    try
                    {
                        int number = Integer.parseInt(input);
                        if(number >= 0)
                        {
                            return null;
                        }
                        return "Cannot be negative";
                    }
                    catch(NumberFormatException e)
                    {
                        return "Must be an integer";
                    }
                });
        autoDeleteThresholdField.setEnabled(settings.isAutoDeleteEnabled());
        JLabel autoDeleteSizeLabel = new JLabel("GB of disk space");
        
        autoDeletePanel.add(autoDeleteCheckbox, "cell 0 0, left");
        autoDeletePanel.add(autoDeleteThresholdField, "cell 1 0, w 30");
        autoDeletePanel.add(autoDeleteSizeLabel, "cell 2 0, grow, left");
        
        BookmarkPanel bookmarkPanel = new BookmarkPanel(settings);
        
        JCheckBox overrideNameCheckbox = new JCheckBox();
        overrideNameCheckbox.setSelected(settings.isOverrideObsNameFormatEnabled());
        overrideNameCheckbox.setText("Override OBS recording name format");
        overrideNameCheckbox.setToolTipText("When enabled, the selected OBS profile will have FilenameFormatting changed before each recording starts to include the name of the game being recorded. This overwrites your OBS profile's existing filename format, so if you use OBS separately from Autorecorder, it will still use the format that Autorecorder sets.");
        autoDeletePanel.add(overrideNameCheckbox, "cell 0 1, spanx"); // TODO this is a hack to make things line up, no need to keep otherwise
        
        JButton licenseButton = new JButton("View License/Terms of Use");
        JButton saveButton = new JButton("Save");
        
        add(obsPathLabel, "cell 0 0");
        add(obsPathField, "cell 0 0, growx");
        add(obsProfileLabel, "cell 1 0");
        add(obsProfileDropdown, "cell 1 0, growx");
        add(recordingPathLabel, "cell 0 1");
        add(recordingPathField, "cell 0 1, growx");
        add(clipPathLabel, "cell 1 1");
        add(clipPathField, "cell 1 1, growx");
        add(additionalGamesPanel, "cell 0 2, grow");
        add(excludedGamesPanel, "cell 1 2, grow");
        add(autoDeletePanel, "cell 0 3");
        add(bookmarkPanel, "cell 1 3");
        add(licenseButton, "cell 0 4, left");
        add(saveButton, "cell 1 4, right, tag apply");
        
        autoDeleteCheckbox.addChangeListener(e -> {
            autoDeleteThresholdField.setEnabled(autoDeleteCheckbox.isSelected());
        });
        
        licenseButton.addActionListener(e -> {
            navigator.showLicenseView();
        });
        
        saveButton.addActionListener(e -> {
            Settings.SettingsContainer tempSettings = new Settings.SettingsContainer();
            tempSettings.obsPath = obsPathField.getText();
            tempSettings.obsProfileName = (String)obsProfileDropdown.getSelectedItem();
            tempSettings.recordingPath = recordingPathField.getText();
            tempSettings.clipPath = clipPathField.getText();
            tempSettings.additionalGames = additionalGamesPanel.getGames();
            tempSettings.excludedGames = excludedGamesPanel.getGames();
            tempSettings.autoDeleteEnabled = autoDeleteCheckbox.isSelected();
            tempSettings.bookmarksEnabled = bookmarkPanel.areBookmarksEnabled();
            tempSettings.bookmarkKey = bookmarkPanel.getBookmarkKey();
            tempSettings.consumeWindowsKeyEnabled = bookmarkPanel.isConsumeWindowsKeyEnabled();
            tempSettings.overrideObsNameFormat = overrideNameCheckbox.isSelected();
            if(autoDeleteThresholdField.isValid())
            {
                tempSettings.autoDeleteThresholdGB = Integer.parseInt(autoDeleteThresholdField.getText());
            }
            else
            {
                tempSettings.autoDeleteThresholdGB = -1;
            }
            
            if(validator.validate(tempSettings))
            {
                settings.setObsPath(obsPathField.getText());
                settings.setObsProfileName((String)obsProfileDropdown.getSelectedItem());
                settings.setRecordingPath(recordingPathField.getText());
                settings.setClipPath(clipPathField.getText());
                settings.setAdditionalGames(additionalGamesPanel.getGames());
                settings.setExcludedGames(excludedGamesPanel.getGames());
                settings.setAutoDeleteEnabled(tempSettings.autoDeleteEnabled);
                settings.setAutoDeleteThresholdGB(tempSettings.autoDeleteThresholdGB);
                settings.setBookmarksEnabled(tempSettings.bookmarksEnabled);
                settings.setBookmarkKey(tempSettings.bookmarkKey);
                settings.setOverrideObsNameFormatEnabled(tempSettings.overrideObsNameFormat);
                settings.setConsumeWindowsKeyEnabled(tempSettings.consumeWindowsKeyEnabled);
                
                settings.save();
                events.postEvent(new Event(EventType.SUCCESS, "Settings saved"));
            }
        });
    }
}
