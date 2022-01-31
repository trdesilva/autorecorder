package io.github.trdesilva.autorecorder.upload;

import io.github.trdesilva.autorecorder.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public abstract class Uploader
{
    protected Settings settings;
    
    public Uploader(Settings settings)
    {
        this.settings = settings;
    }
    
    // should return the URL of the uploaded video
    public abstract String upload(String clipName, String videoTitle, String description) throws IOException;
    
    protected File getClip(String clipName)
    {
        return Paths.get(settings.getClipPath(), clipName).toFile();
    }
}
