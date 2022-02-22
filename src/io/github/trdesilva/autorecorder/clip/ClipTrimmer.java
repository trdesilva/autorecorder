/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ClipTrimmer
{
    private final Settings settings;
    private final VideoListHandler clipListHandler;
    private final VideoListHandler recordingListHandler;
    
    @Inject
    public ClipTrimmer(Settings settings, @Named("CLIP") VideoListHandler clipListHandler,
                       @Named("RECORDING") VideoListHandler recordingListHandler)
    {
        this.settings = settings;
        this.clipListHandler = clipListHandler;
        this.recordingListHandler = recordingListHandler;
    }
    
    public void makeClip(String source, String dest, double start, double end) throws IOException, InterruptedException
    {
        Duration startDuration = Duration.ofMillis((long) (start * 1000));
        String startArg = String.format("%02d:%02d:%02d.%03d", startDuration.toHoursPart(),
                                        startDuration.toMinutesPart(),
                                        startDuration.toSecondsPart(), startDuration.toMillisPart());
        Duration endDuration = Duration.ofMillis((long) (end * 1000));
        String endArg = String.format("%02d:%02d:%02d.%03d", endDuration.toHoursPart(), endDuration.toMinutesPart(),
                                      endDuration.toSecondsPart(), endDuration.toMillisPart());
        
        makeClip(source, dest, startArg, endArg);
    }
    
    public void makeClip(String source, String dest, String startArg, String endArg) throws IOException,
                                                                                            InterruptedException
    {
        File sourceFile = recordingListHandler.getVideo(source);
        File destFile = clipListHandler.getVideo(dest);
        
        if(sourceFile == null || destFile == null)
        {
            throw new IOException("Bad source or destination");
        }
        
        if(destFile.exists())
        {
            throw new IOException("Destination already exists");
        }
        
        String[] ffmpegArgs = {settings.getFfmpegPath(), "-i", sourceFile.getAbsolutePath(), "-ss", startArg, "-to", endArg, "-c", "copy", destFile.getAbsolutePath()};
        Process ffmpegProc = Runtime.getRuntime()
                                    .exec(ffmpegArgs, null,
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
        if(ffmpegResult != 0)
        {
            throw new IOException(ffmpegError);
        }
    }
}
