/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.cli;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;

import java.io.File;
import java.util.Set;

public class SettingsCli extends Cli
{
    
    public static final String OBS_FILEPATH = "OBS filepath";
    public static final String RECORDING_DIRECTORY = "Recording directory";
    public static final String FFMPEG_FILEPATH = "FFMpeg filepath";
    public static final String CLIP_DIRECTORY = "Clip directory";
    public static final String ADDITIONAL_GAMES = "Additional games";
    public static final String EXCLUDED_GAMES = "Excluded games";
    
    @Inject
    public SettingsCli(Settings settings)
    {
        super(settings);
    }
    
    @Override
    public void run()
    {
        print("Settings are stored at %s", settings.getSettingsFilePath());
        while(true)
        {
            String setting = chooseFromList("Choose a setting to change",
                                            OBS_FILEPATH,
                                            RECORDING_DIRECTORY,
                                            CLIP_DIRECTORY,
                                            ADDITIONAL_GAMES,
                                            EXCLUDED_GAMES);
            if(setting.equals("exit"))
            {
                return;
            }
            if(setting.equals(OBS_FILEPATH) || setting.equals(RECORDING_DIRECTORY) || setting.equals(
                    FFMPEG_FILEPATH) || setting.equals(CLIP_DIRECTORY))
            {
                print("Enter a new value for %s", setting);
                String newValue = readLine();
                File newValueFile = new File(newValue);
                if(!newValueFile.exists())
                {
                    print("%s does not exist", newValue);
                    continue;
                }
                if(setting.equals(OBS_FILEPATH))
                {
                    settings.setObsPath(newValue);
                }
                else
                {
                    if(!newValueFile.isDirectory())
                    {
                        print("%s is not a directory", newValue);
                        continue;
                    }
                    if(setting.equals(RECORDING_DIRECTORY))
                    {
                        settings.setRecordingPath(newValue);
                    }
                    else if(setting.equals(CLIP_DIRECTORY))
                    {
                        settings.setClipPath(newValue);
                    }
                }
            }
            else if(setting.equals(ADDITIONAL_GAMES))
            {
                editGameSet(settings.getAdditionalGames());
            }
            else if(setting.equals(EXCLUDED_GAMES))
            {
                editGameSet(settings.getExcludedGames());
            }
            
            settings.save();
        }
    }
    
    private void editGameSet(Set<String> games)
    {
        print(games.toString());
        String mode = chooseFromList("Add or remove?", "add", "remove");
        if(mode.equals("add"))
        {
            print("Enter a name of or relative path to a game executable");
            games.add(readLine());
        }
        else if(mode.equals("remove"))
        {
            String game = chooseFromList("Choose a game to remove", games.toArray(new String[0]));
            games.remove(game);
        }
    }
}
