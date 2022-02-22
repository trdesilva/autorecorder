/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload;

import io.github.trdesilva.autorecorder.ui.gui.ReportableException;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public abstract class Uploader
{
    protected final VideoListHandler clipListHandler;
    
    public Uploader(VideoListHandler clipListHandler)
    {
        this.clipListHandler = clipListHandler;
    }
    
    // should return the URL of the uploaded video
    public abstract String upload(UploadJob job) throws IOException, ReportableException;
    
    public abstract UploadJobValidator getValidator();
    
    protected File getClip(String clipName)
    {
        return clipListHandler.getVideo(clipName);
    }
}
