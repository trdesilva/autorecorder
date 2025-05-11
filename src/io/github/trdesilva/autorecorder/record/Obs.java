/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.record;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Obs
{
    public static final Path PROFILES_DIR = Paths.get(System.getenv("APPDATA")).resolve("obs-studio").resolve("basic").resolve("profiles");
    public static final String PROFILE_CONFIG_FILENAME = "basic.ini";
    
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
                String[] obsArgs = {settings.getObsPath(), "--startrecording", "--minimize-to-tray", "--disable-updater",
                        "--disable-shutdown-check", "--profile", String.format("\"%s\"", settings.getObsProfileName())};
                
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
    
    public List<String> readProfileNames()
    {
        File[] maybeProfileDirs = PROFILES_DIR.toFile().listFiles();
        if(maybeProfileDirs != null)
        {
            List<String> result = new ArrayList<>(maybeProfileDirs.length);
            for(File maybeDir: maybeProfileDirs)
            {
                if(maybeDir.canRead() && maybeDir.isDirectory())
                {
                    result.add(maybeDir.getName());
                }
            }
            return result;
        }
        
        return Collections.emptyList();
    }
    
    public File getActiveProfileConfigFile()
    {
        return PROFILES_DIR.resolve(settings.getObsProfileName()).resolve(PROFILE_CONFIG_FILENAME).toFile();
    }
}
