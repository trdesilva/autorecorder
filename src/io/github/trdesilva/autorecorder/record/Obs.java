package io.github.trdesilva.autorecorder.record;

import io.github.trdesilva.autorecorder.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class Obs
{
    Settings settings;
    
    AtomicBoolean recording;
    
    Process process;
    
    public Obs(Settings settings)
    {
        this.settings = settings;
        
        recording = new AtomicBoolean(false);
    }
    
    public synchronized void start() throws IOException
    {
        if(!recording.get())
        {
            String obsDir = Paths.get(settings.getObsPath()).getParent().toString();
            String[] obsArgs = {settings.getObsPath(), "--startrecording", "--minimize-to-tray"};
            System.out.println("starting recording");
            process = Runtime.getRuntime().exec(obsArgs, null, new File(obsDir));
            System.out.println("recording started");
            recording.set(true);
        }
    }
    
    public synchronized void stop()
    {
        if(recording.get())
        {
            System.out.println("stopping recording");
            // OBS prompts if you don't force kill
            process.destroyForcibly();
            System.out.println("recording stopped");
            recording.set(false);
        }
    }
}
