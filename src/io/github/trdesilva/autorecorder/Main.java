/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import io.github.trdesilva.autorecorder.record.GameListener;
import io.github.trdesilva.autorecorder.record.Obs;
import io.github.trdesilva.autorecorder.ui.cli.MainCli;
import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("starting");
        Settings settings = new Settings();
        settings.populate();
    
        Obs obs = new Obs(settings);
        GameListener listener = new GameListener(obs, settings);
        
        if(args.length >= 1 && args[0].equals("-cli"))
        {
            System.out.println("running in CLI mode");
            listener.start();
    
            YoutubeUploader uploader = new YoutubeUploader(settings);
            MainCli cli = new MainCli(settings, uploader);
            cli.run();
            
            listener.stop();
        }
        else
        {
            MainWindow mainWindow = new MainWindow(settings);
        }
    }
}
