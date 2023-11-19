/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.ReportableException;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;
import io.github.trdesilva.autorecorder.upload.Uploader;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class YoutubeUploader extends Uploader
{
    public static String PRIVACY_PROPERTY = "privacyStatus";
    
    public static final String VIDEO_URL_PREFIX = "https://www.youtube.com/watch?v=";
    
    private final YoutubeJobValidator validator;
    private final YoutubeJsonErrorParser errorParser;
    private final YoutubeServiceFactory youtubeServiceFactory;
    private final EventQueue events;
    
    @Inject
    public YoutubeUploader(@Named("CLIP") VideoListHandler clipListHandler, YoutubeJobValidator validator,
                           YoutubeJsonErrorParser errorParser, YoutubeServiceFactory youtubeServiceFactory,
                           EventQueue events)
    {
        super(clipListHandler);
        this.validator = validator;
        this.errorParser = errorParser;
        this.youtubeServiceFactory = youtubeServiceFactory;
        this.events = events;
    }
    
    @Override
    public String upload(UploadJob uploadJob) throws IOException, ReportableException
    {
        return upload(uploadJob.getClipName(), uploadJob.getVideoTitle(), uploadJob.getDescription(),
                      PrivacyStatus.valueOf(uploadJob.getProperty(PRIVACY_PROPERTY)));
    }
    
    @Override
    public UploadJobValidator getValidator()
    {
        return validator;
    }
    
    public String upload(String clipName, String videoTitle,
                         String description, PrivacyStatus privacyStatus) throws IOException, ReportableException
    {
        try
        {
            events.postEvent(new Event(EventType.DEBUG,
                                       String.format("uploading clip: %s\ttitle: %s\tdescription: %s\tprivacy: %s",
                                                     clipName, videoTitle, description,
                                                     privacyStatus.name())));
            YouTube youtubeService = youtubeServiceFactory.getService();
            
            // Define the Video object, which will be uploaded as the request body.
            Video video = new Video();
            
            // Add the snippet object property to the Video object.
            VideoSnippet snippet = new VideoSnippet();
            // gaming category
            snippet.setCategoryId("20");
            
            snippet.setDescription(description);
            
            snippet.setTitle(videoTitle);
            
            video.setSnippet(snippet);
            
            // Add the status object property to the Video object.
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus(privacyStatus.name().toLowerCase());
            status.setSelfDeclaredMadeForKids(false);
            video.setStatus(status);
            
            File mediaFile = getClip(clipName);
            InputStreamContent mediaContent =
                    new InputStreamContent("application/octet-stream",
                                           new BufferedInputStream(new FileInputStream(mediaFile)));
            mediaContent.setLength(mediaFile.length());
            
            // Define and execute the API request
            YouTube.Videos.Insert request = youtubeService.videos()
                                                          .insert(Arrays.asList("snippet", "status"), video,
                                                                  mediaContent);
            
            try
            {
                Video response = request.execute();
                return String.format(VIDEO_URL_PREFIX + "%s", response.getId());
            }
            catch(GoogleJsonResponseException e)
            {
                throw errorParser.parseError(e);
            }
            catch(TokenResponseException e)
            {
                throw new ReportableException("Google authentication failed: " + e.getDetails().getErrorDescription(),
                                              e);
            }
        }
        catch(GeneralSecurityException e)
        {
            throw new IOException(e);
        }
    }
}
