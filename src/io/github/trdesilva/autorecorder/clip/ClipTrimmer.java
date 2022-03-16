/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClipTrimmer
{
    private final Settings settings;
    private final VideoListHandler clipListHandler;
    private final VideoListHandler recordingListHandler;
    private final EventQueue events;
    
    @Inject
    public ClipTrimmer(Settings settings, @Named("CLIP") VideoListHandler clipListHandler,
                       @Named("RECORDING") VideoListHandler recordingListHandler, EventQueue events)
    {
        this.settings = settings;
        this.clipListHandler = clipListHandler;
        this.recordingListHandler = recordingListHandler;
        this.events = events;
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
        
        List<String> ffmpegArgs = getSingleClipArgs(sourceFile.getAbsolutePath(), destFile.getAbsolutePath(), startArg, endArg, true);
        runFfmpeg(ffmpegArgs);
    }
    
    public void makeSegmentedClip(String source, String dest, List<String> startArgs, List<String> endArgs) throws IOException, InterruptedException
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
        
        List<String> segmentedClipArgs = new LinkedList<>();
        StringBuilder filterBuilder = new StringBuilder();
        
        for(int i = 0; i < startArgs.size(); i++)
        {
            // validator should have already verified that startArgs and endArgs are the same size
            String tempClipName = getTempClipName(destFile.getAbsolutePath(), i);
            List<String> ffmpegArgs = getSingleClipArgs(sourceFile.getAbsolutePath(), tempClipName, startArgs.get(i), endArgs.get(i), true);
            runFfmpeg(ffmpegArgs);
            segmentedClipArgs.add("-i");
            segmentedClipArgs.add(tempClipName);
            
            filterBuilder.append(String.format("[%d:v] [%d:a] ", i, i));
        }
    
        filterBuilder.append(String.format("concat=n=%d:v=1:a=1 [v] [a]", startArgs.size()));
        segmentedClipArgs.addAll(Arrays.asList("-filter_complex", filterBuilder.toString(), "-map", "[v]", "-map", "[a]", destFile.getAbsolutePath()));
        runFfmpeg(segmentedClipArgs);
        
        for(int i = 0; i < startArgs.size(); i++)
        {
            if(!Paths.get(getTempClipName(destFile.getAbsolutePath(), i)).toFile().delete())
            {
                events.postEvent(new Event(EventType.WARNING, "Failed to delete temporary clip segment file " + getTempClipName(destFile.getAbsolutePath(), i)));
            }
        }
    }
    
    private void runFfmpeg(List<String> ffmpegArgs) throws IOException, InterruptedException
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
    
    private String getTempClipName(String dest, int number)
    {
        Path destPath = Paths.get(dest);
        return destPath.resolveSibling(String.format("temp%d_%s", number, destPath.getFileName())).toString();
    }
    
    private LinkedList<String> getSingleClipArgs(String sourceFilePath, String destFilePath, String startArg, String endArg)
    {
        return getSingleClipArgs(sourceFilePath, destFilePath, startArg, endArg, false);
    }
    
    private LinkedList<String> getSingleClipArgs(String sourceFilePath, String destFilePath, String startArg, String endArg, boolean reencode)
    {
        if(reencode)
        {
            String relativeEndArg = TimestampUtil.formatTime(TimestampUtil.parseTime(endArg) - TimestampUtil.parseTime(startArg));
            return new LinkedList<>(
                    Arrays.asList("-ss", startArg, "-i", sourceFilePath, "-to", relativeEndArg, "-vcodec", "h264_nvenc", "-acodec", "aac", destFilePath));
        }
        else
        {
            return new LinkedList<>(
                    Arrays.asList("-i", sourceFilePath, "-ss", startArg, "-to", endArg, "-c", "copy", destFilePath));
        }
    }
}
