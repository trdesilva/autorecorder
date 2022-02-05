/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

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
    public abstract String upload(UploadJob job) throws IOException;
    
    public abstract UploadJobValidator getValidator();
    
    protected File getClip(String clipName)
    {
        if(!clipName.startsWith(settings.getClipPath()))
        {
            return Paths.get(settings.getClipPath()).resolve(clipName).toFile();
        }
        
        return new File(clipName);
    }
}
