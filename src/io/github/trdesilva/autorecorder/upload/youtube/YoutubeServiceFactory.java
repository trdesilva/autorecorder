/*
 * Copyright (c) 2023 Thomas DeSilva.
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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.ReportableException;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

public class YoutubeServiceFactory
{
    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.upload",
                          "https://www.googleapis.com/auth/youtube.readonly");
    
    private static final String APPLICATION_NAME = "Autorecorder";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private static DataStoreFactory DATA_STORE_FACTORY;
    
    private final EventQueue events;
    
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
    
    @Inject
    public YoutubeServiceFactory(EventQueue events)
    {
        this.events = events;
    }
    
    public YouTube getService() throws GeneralSecurityException, IOException, ReportableException
    {
        File clientSecretFile = Settings.SETTINGS_DIR.resolve(CLIENT_SECRETS).toFile();
        if(!clientSecretFile.exists() || !clientSecretFile.canRead())
        {
            throw new ReportableException("Client secret file doesn't exist or isn't accessible");
        }
        
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
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
        
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
