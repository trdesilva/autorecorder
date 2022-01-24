package io.github.trdesilva.autorecorder.ui.cli;

import io.github.trdesilva.autorecorder.Settings;

import java.io.File;

public class SettingsCli extends Cli
{
    
    public static final String OBS_FILEPATH = "OBS filepath";
    public static final String RECORDING_DIRECTORY = "Recording directory";
    public static final String FFMPEG_FILEPATH = "FFMpeg filepath";
    public static final String CLIP_DIRECTORY = "Clip directory";
    
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
                                            FFMPEG_FILEPATH,
                                            CLIP_DIRECTORY);
            if(setting.equals("exit"))
            {
                return;
            }
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
            else if(setting.equals(FFMPEG_FILEPATH))
            {
                settings.setFfmpegPath(newValue);
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
            
            settings.save();
        }
    }
}
