package io.github.trdesilva.autorecorder.ui.cli;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.Uploader;

import java.io.File;
import java.io.IOException;

public class UploaderCli extends Cli
{
    private Uploader uploader;
    
    public UploaderCli(Settings settings, Uploader uploader)
    {
        super(settings);
        
        this.uploader = uploader;
    }
    
    @Override
    public void run()
    {
        File clipDir = new File(settings.getClipPath());
        while(true)
        {
            if(clipDir.exists() && clipDir.isDirectory())
            {
                String[] clips = clipDir.list();
                if(clips != null && clips.length > 0)
                {
                    String clip = chooseFromList("Choose a clip to upload", clips);
                    if(clip.equals("exit"))
                    {
                        return;
                    }
                    
                    print("Enter a video title (default: \"%s\")", clip);
                    String videoTitle = readLine();
                    if(videoTitle.isBlank())
                    {
                        videoTitle = clip;
                    }
                    
                    print("Enter a video description");
                    String description = readLine();
                    
                    try
                    {
                        String url = uploader.upload(clip, videoTitle, description);
                        print("Video uploaded to " + url);
                    }
                    catch(IOException e)
                    {
                        print("Upload failed");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
