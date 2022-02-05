/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;
import io.github.trdesilva.autorecorder.upload.Uploader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

public class YoutubeUploader extends Uploader
{
    public static String PRIVACY_PROPERTY = "privacyStatus";
    
    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.upload");
    
    private static final String APPLICATION_NAME = "Autorecorder";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String VIDEO_URL_FORMAT = "https://www.youtube.com/watch?v=%s";
    
    public YoutubeUploader(Settings settings)
    {
        super(settings);
    }
    
    @Override
    public String upload(UploadJob uploadJob) throws IOException
    {
        return upload(uploadJob.getClipName(), uploadJob.getVideoTitle(), uploadJob.getDescription(), PrivacyStatus.valueOf(uploadJob.getProperty(PRIVACY_PROPERTY)));
    }
    
    @Override
    public UploadJobValidator getValidator()
    {
        return new YoutubeJobValidator();
    }
    
    public String upload(String clipName, String videoTitle, String description, PrivacyStatus privacyStatus) throws IOException
    {
        try
        {
            YouTube youtubeService = getService();
            
            // Define the Video object, which will be uploaded as the request body.
            Video video = new Video();
            
            // Add the snippet object property to the Video object.
            VideoSnippet snippet = new VideoSnippet();
            // gaming category
            snippet.setCategoryId("20");
            
            description = description.strip().replace("<", "lt").replace(">", "gt");
            if(description.length() > 5000) description = description.substring(0, 5000);
            snippet.setDescription(description);
            
            videoTitle = videoTitle.strip().replace("<", "lt").replace(">", "gt");
            if(videoTitle.length() > 100) videoTitle = videoTitle.substring(0, 100);
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
                                                          .insert("snippet,status", video, mediaContent);
            Video response = request.execute();
            System.out.println(response);
            
            return String.format(VIDEO_URL_FORMAT, response.getId());
        }
        catch(GeneralSecurityException e)
        {
            System.out.println("Google API security error");
            e.printStackTrace();
            throw new IOException(e);
        }
    }
    
    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize(final NetHttpTransport httpTransport) throws IOException
    {
        // Load client secrets.
        InputStream in = new FileInputStream(Settings.SETTINGS_DIR.resolve(CLIENT_SECRETS).toFile());
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }
    
    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private static YouTube getService() throws GeneralSecurityException, IOException
    {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
