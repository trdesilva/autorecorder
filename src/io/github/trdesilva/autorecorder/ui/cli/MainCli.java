/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.cli;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;

public class MainCli extends Cli
{
    private final ClipTrimmerCli clipTrimmerCli;
    private final SettingsCli settingsCli;
    private final UploaderCli uploaderCli;
    
    @Inject
    public MainCli(Settings settings, ClipTrimmerCli clipTrimmerCli, SettingsCli settingsCli, UploaderCli uploaderCli)
    {
        super(settings);
        this.clipTrimmerCli = clipTrimmerCli;
        this.settingsCli = settingsCli;
        this.uploaderCli = uploaderCli;
    }
    
    @Override
    public void run()
    {
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
