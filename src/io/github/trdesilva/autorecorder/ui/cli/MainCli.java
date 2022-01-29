package io.github.trdesilva.autorecorder.ui.cli;

import io.github.trdesilva.autorecorder.Settings;

public class MainCli extends Cli
{
    public MainCli(Settings settings)
    {
        super(settings);
    }
    
    @Override
    public void run()
    {
        ClipTrimmerCli clipTrimmerCli = new ClipTrimmerCli(settings);
        SettingsCli settingsCli = new SettingsCli(settings);
        while(true)
        {
            print("Main menu");
            String answer = chooseFromList("Pick something:", "clip", "settings");
            if(answer.equals("clip"))
            {
                clipTrimmerCli.run();
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
