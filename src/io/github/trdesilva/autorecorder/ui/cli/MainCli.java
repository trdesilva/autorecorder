package io.github.trdesilva.autorecorder.ui.cli;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.Uploader;

public class MainCli extends Cli
{
    private Uploader uploader;
    
    public MainCli(Settings settings, Uploader uploader)
    {
        super(settings);
        this.uploader = uploader;
    }
    
    @Override
    public void run()
    {
        ClipTrimmerCli clipTrimmerCli = new ClipTrimmerCli(settings);
        SettingsCli settingsCli = new SettingsCli(settings);
        UploaderCli uploaderCli = new UploaderCli(settings, uploader);
        while(true)
        {
            print("Main menu");
            String answer = chooseFromList("Pick something:", "clip", "upload", "settings");
            if(answer.equals("clip"))
            {
                clipTrimmerCli.run();
            }
            else if(answer.equals("upload"))
            {
                uploaderCli.run();
            }
            else if(answer.equals("settings"))
            {
                settingsCli.run();
            }
            else if(answer.equals("exit"))
            {
                return;
            }
        }
    }
}
