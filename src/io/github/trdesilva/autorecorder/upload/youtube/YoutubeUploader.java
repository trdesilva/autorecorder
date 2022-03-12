/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.ReportableException;
import io.github.trdesilva.autorecorder.upload.UploadJob;
import io.github.trdesilva.autorecorder.upload.UploadJobValidator;
import io.github.trdesilva.autorecorder.upload.Uploader;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import java.awt.Desktop;
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
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String VIDEO_URL_FORMAT = "https://www.youtube.com/watch?v=%s";
    private static DataStoreFactory DATA_STORE_FACTORY;
    
    static
    {
        try
        {
            DATA_STORE_FACTORY = new FileDataStoreFactory(Settings.SETTINGS_DIR.toFile());
        }
        catch(IOException e)
        {
            System.out.println("DataStoreFactory failed to open settings dir");
            e.printStackTrace();
        }
    }
    
    private final YoutubeJobValidator validator;
    private final YoutubeJsonErrorParser errorParser;
    private final EventQueue events;
    
    private YouTube youtubeService;
    
    @Inject
    public YoutubeUploader(@Named("CLIP") VideoListHandler clipListHandler, YoutubeJobValidator validator,
                           YoutubeJsonErrorParser errorParser,
                           EventQueue events) throws IOException
    {
        super(clipListHandler);
        this.validator = validator;
        this.errorParser = errorParser;
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
        File clientSecretFile = Settings.SETTINGS_DIR.resolve(CLIENT_SECRETS).toFile();
        if(!clientSecretFile.exists() || !clientSecretFile.canRead())
        {
            throw new ReportableException("Client secret file doesn't exist or isn't accessible");
        }
        
        try
        {
            events.postEvent(new Event(EventType.DEBUG,
                                       String.format("uploading clip: %s\ttitle: %s\tdescription: %s\tprivacy: %s",
                                                     clipName, videoTitle, description,
                                                     privacyStatus.name())));
            ensureCredential();
            
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
                return String.format(VIDEO_URL_FORMAT, response.getId());
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
    
    private void ensureCredential() throws GeneralSecurityException, IOException
    {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        File clientSecretFile = Settings.SETTINGS_DIR.resolve(CLIENT_SECRETS).toFile();
        InputStream in = new FileInputStream(clientSecretFile);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
                                                                                   clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        Credential credential = flow.loadCredential("user");
        boolean goodCredential;
        try
        {
            if(credential != null)
            {
                credential.refreshToken();
                goodCredential = true;
                events.postEvent(new Event(EventType.DEBUG, "successfully refreshed token"));
            }
            else
            {
                goodCredential = false;
            }
        }
        catch(TokenResponseException e)
        {
            goodCredential = false;
        }
        
        if(!goodCredential)
        {
            events.postEvent(new Event(EventType.DEBUG, "bad/missing YouTube cred, starting OAuth flow"));
            LocalServerReceiver codeReceiver = new LocalServerReceiver();
            String redirectUri = codeReceiver.getRedirectUri();
            Desktop.getDesktop().browse(flow.newAuthorizationUrl().setRedirectUri(redirectUri).toURI());
            String code = codeReceiver.waitForCode();
            GoogleAuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code).setRedirectUri(redirectUri);
            credential = flow.createAndStoreCredential(tokenRequest.execute(), "user");
            events.postEvent(new Event(EventType.DEBUG, "OAuth flow done"));
        }
        
        youtubeService = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
