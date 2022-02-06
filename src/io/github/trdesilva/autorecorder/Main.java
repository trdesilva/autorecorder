/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import io.github.trdesilva.autorecorder.record.GameListener;
import io.github.trdesilva.autorecorder.record.Obs;
import io.github.trdesilva.autorecorder.ui.cli.MainCli;
import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("starting");
        Settings settings = new Settings();
        settings.populate();
        if(!new File(settings.getFfmpegPath()).exists())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "copying ffmpeg.exe from resources"));
            try
            {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("ffmpeg.exe"),
                           Settings.SETTINGS_DIR.resolve("ffmpeg.exe"));
            }
            catch(IOException e)
            {
                StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "failed to copy ffmpeg.exe"));
            }
        }
        if(!new File(settings.getFfprobePath()).exists())
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "copying ffprobe.exe from resources"));
            try
            {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("ffprobe.exe"),
                           Settings.SETTINGS_DIR.resolve("ffprobe.exe"));
            }
            catch(IOException e)
            {
                StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "failed to copy ffprobe.exe"));
            }
        }
    
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
