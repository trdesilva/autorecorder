/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.cli;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.upload.youtube.PrivacyStatus;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;

import java.io.File;
import java.io.IOException;

public class UploaderCli extends Cli
{
    private final YoutubeUploader uploader;
    
    @Inject
    public UploaderCli(Settings settings, YoutubeUploader uploader)
    {
        super(settings);
        
        this.uploader = uploader;
    }
    
    @Override
    public void run()
    {
        File clipDir = new File(settings.getClipPath());
        print("By clicking 'Upload,' you certify that the content you are uploading complies with the YouTube Terms of Service (including the YouTube Community Guidelines) at https://www.youtube.com/t/terms. Please be sure not to violate others' copyright or privacy rights.");
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
                    
                    String privacy = chooseFromList("Choose a privacy setting", PrivacyStatus.PRIVATE.name(),
                                                    PrivacyStatus.UNLISTED.name(), PrivacyStatus.PUBLIC.name());
                    
                    try
                    {
                        String url = uploader.upload(clip, videoTitle, description, PrivacyStatus.valueOf(privacy));
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
