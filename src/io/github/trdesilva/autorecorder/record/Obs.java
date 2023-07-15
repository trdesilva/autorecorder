/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.record;

import com.google.inject.Inject;
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
    
    @Inject
    public Obs(Settings settings)
    {
        this.settings = settings;
        
        recording = new AtomicBoolean(false);
    }
    
    public synchronized void start() throws IOException
    {
        if(!recording.get())
        {
            if(settings.getObsPath() != null && !settings.getObsPath().isBlank())
            {
                String obsDir = Paths.get(settings.getObsPath()).getParent().toString();
                String[] obsArgs = {settings.getObsPath(), "--startrecording", "--minimize-to-tray", "--disable-updater"};
                process = Runtime.getRuntime().exec(obsArgs, null, new File(obsDir));
                recording.set(true);
            }
            else
            {
                throw new IOException("OBS Path is not set");
            }
        }
    }
    
    public synchronized void stop()
    {
        if(recording.get())
        {
            // OBS prompts if you don't force kill
            process.destroyForcibly();
            recording.set(false);
        }
    }
}
