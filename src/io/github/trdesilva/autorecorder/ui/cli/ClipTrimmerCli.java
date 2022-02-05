/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.cli;

import io.github.trdesilva.autorecorder.clip.ClipTrimmer;
import io.github.trdesilva.autorecorder.Settings;

import java.io.File;

public class ClipTrimmerCli extends Cli
{
    private ClipTrimmer clipTrimmer;
    
    public ClipTrimmerCli(Settings settings)
    {
        super(settings);
        
        clipTrimmer = new ClipTrimmer(settings);
    }
    
    @Override
    public void run()
    {
        File recordingDir = new File(settings.getRecordingPath());
        while(true)
        {
            if(recordingDir.exists() && recordingDir.isDirectory())
            {
                String[] recordings = recordingDir.list();
                if(recordings != null && recordings.length > 0)
                {
                    String recording = chooseFromList("Choose a recording to clip", recordings);
                    if(recording.equals("exit"))
                    {
                        return;
                    }
                    print("Enter a name for the clip");
                    String clipName = readLine();
                    print("Enter a start time (format hh:mm:ss(.SSS))");
                    String start = readLine();
                    print("Enter an end time (format hh:mm:ss(.SSS))");
                    String end = readLine();
                    
                    String timestampRegex = "\\d\\d:\\d\\d:\\d\\d(?:\\.\\d\\d\\d)?";
                    if(start.matches(timestampRegex) && end.matches(timestampRegex))
                    {
                        try
                        {
                            String recordingExtension = recording.substring(recording.lastIndexOf('.'));
                            print("Clipping...");
                            clipTrimmer.makeClip(recording, clipName + recordingExtension, start, end);
                            print("Clip %s created in %s", clipName, settings.getClipPath());
                        }
                        catch(Exception e)
                        {
                            print("Clipping failed");
                            e.printStackTrace();
                            return;
                        }
                    }
                    else
                    {
                        print("Start or end time isn't properly formatted");
                    }
                }
                else
                {
                    print("No recordings in %s to clip", settings.getRecordingPath());
                    return;
                }
            }
            else
            {
                print("Recording directory %s is invalid", settings.getRecordingPath());
                return;
            }
        }
    }
}
