/*
 * Copyright (c) 2023 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FfmpegHelper
{
    private final Settings settings;
    
    @Inject
    public FfmpegHelper(Settings settings)
    {
        this.settings = settings;
    }
    
    public void runFfmpeg(List<String> ffmpegArgs) throws IOException, InterruptedException
    {
        ffmpegArgs.add(0, settings.getFfmpegPath());
        Process ffmpegProc = Runtime.getRuntime()
                                    .exec(ffmpegArgs.toArray(new String[0]), null,
                                          new File(Paths.get(settings.getFfmpegPath()).getParent().toString()));
        InputStream stdout = ffmpegProc.getInputStream();
        InputStream stderr = ffmpegProc.getErrorStream();
        String ffmpegOutput = "";
        String ffmpegError = "";
        int ffmpegResult;
        // ffmpeg hangs if you don't read its stdout
        while(!ffmpegProc.waitFor(1000, TimeUnit.MILLISECONDS))
        {
            if(stdout.available() > 0)
            {
                ffmpegOutput += IOUtils.toString(stdout, Charset.defaultCharset());
            }
            if(stderr.available() > 0)
            {
                ffmpegError += IOUtils.toString(stderr, Charset.defaultCharset());
            }
        }
        ffmpegResult = ffmpegProc.waitFor();
        if(stdout.available() > 0)
        {
            ffmpegOutput += IOUtils.toString(stdout, Charset.defaultCharset());
        }
        if(stderr.available() > 0)
        {
            ffmpegError += IOUtils.toString(stderr, Charset.defaultCharset());
        }
        if(ffmpegResult != 0)
        {
            throw new IOException(ffmpegError);
        }
    }
}
