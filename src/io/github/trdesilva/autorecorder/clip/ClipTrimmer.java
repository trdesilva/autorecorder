/*
 * Copyright (c) 2022 Thomas DeSilva.
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
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ClipTrimmer
{
    private final Settings settings;
    
    @Inject
    public ClipTrimmer(Settings settings)
    {
        this.settings = settings;
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
        if(!source.startsWith(settings.getRecordingPath()))
        {
            source = Paths.get(settings.getRecordingPath()).resolve(source).toString();
        }
        
        if(!dest.startsWith(settings.getClipPath()))
        {
            dest = Paths.get(settings.getClipPath()).resolve(dest).toString();
        }
        
        
        if(new File(dest).exists())
        {
            throw new IOException("Destination already exists");
        }
        
        String[] ffmpegArgs = {settings.getFfmpegPath(), "-i", source, "-ss", startArg, "-to", endArg, "-c", "copy", dest};
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
            System.out.println("FFMpeg failed:\n" + ffmpegError);
            throw new IOException(ffmpegError);
        }
    }
}
