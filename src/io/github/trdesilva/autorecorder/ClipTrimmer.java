package io.github.trdesilva.autorecorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

public class ClipTrimmer
{
    private Settings settings;
    
    public ClipTrimmer(Settings settings)
    {
        this.settings = settings;
    }
    
    public void makeClip(String source, String dest, double start, double end) throws IOException, InterruptedException
    {
        Duration startDuration = Duration.ofMillis((long) (start * 1000));
        String startArg = String.format("%02d:%02d:%02d.%03d", startDuration.toHoursPart(), startDuration.toMinutesPart(),
                                        startDuration.toSecondsPart(), startDuration.toMillisPart());
        Duration endDuration = Duration.ofMillis((long) (end * 1000));
        String endArg = String.format("%02d:%02d:%02d.%03d", endDuration.toHoursPart(), endDuration.toMinutesPart(),
                                      endDuration.toSecondsPart(), endDuration.toMillisPart());
        
        makeClip(source, dest, startArg, endArg);
    }
    
    public void makeClip(String source, String dest, String startArg, String endArg) throws IOException, InterruptedException
    {
        if(!source.startsWith(settings.getRecordingPath()))
        {
            source = Paths.get(settings.getRecordingPath()).resolve(source).toString();
        }
        
        if(!dest.startsWith(settings.getClipPath()))
        {
            dest = Paths.get(settings.getClipPath()).resolve(dest).toString();
        }
        
        String[] ffmpegArgs = {settings.getFfmpegPath(), "-i", source, "-ss", startArg, "-to", endArg, "-c", "copy", dest};
        Process ffmpegProc = Runtime.getRuntime().exec(ffmpegArgs, null, new File(Paths.get(settings.getFfmpegPath()).getParent().toString()));
        
        System.out.println("ffmpeg returned " + ffmpegProc.waitFor());
    }
}
