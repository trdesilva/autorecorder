/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.trdesilva.autorecorder.record.GameListener;
import io.github.trdesilva.autorecorder.ui.cli.MainCli;
import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.ui.gui.WindowCloseHandler;
import io.github.trdesilva.autorecorder.ui.gui.inject.GuiModule;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("starting");
    
        Injector injector = Guice.createInjector(new GuiModule());
        
        Settings settings = injector.getInstance(Settings.class);
        settings.populate();
        
        StatusQueue status = injector.getInstance(StatusQueue.class);
        
        if(!new File(settings.getFfmpegPath()).exists())
        {
            status.postMessage(new StatusMessage(StatusType.DEBUG, "copying ffmpeg.exe from resources"));
            try
            {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("ffmpeg.exe"),
                           Settings.SETTINGS_DIR.resolve("ffmpeg.exe"));
            }
            catch(IOException e)
            {
                status.postMessage(new StatusMessage(StatusType.DEBUG, "failed to copy ffmpeg.exe"));
            }
        }
        if(!new File(settings.getFfprobePath()).exists())
        {
            status.postMessage(new StatusMessage(StatusType.DEBUG, "copying ffprobe.exe from resources"));
            try
            {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("ffprobe.exe"),
                           Settings.SETTINGS_DIR.resolve("ffprobe.exe"));
            }
            catch(IOException e)
            {
                status.postMessage(new StatusMessage(StatusType.DEBUG, "failed to copy ffprobe.exe"));
            }
        }
    
        GameListener listener = injector.getInstance(GameListener.class);
        
        if(args.length >= 1 && args[0].equals("-cli"))
        {
            System.out.println("running in CLI mode");
            listener.start();
    
            MainCli cli = injector.getInstance(MainCli.class);
            cli.run();
            
            listener.stop();
        }
        else
        {
            MainWindow mainWindow = injector.getInstance(MainWindow.class);
            mainWindow.start();
        }
    }
}
