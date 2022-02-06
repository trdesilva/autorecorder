/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Settings
{
    public static final Path SETTINGS_DIR = Paths.get(System.getenv("LOCALAPPDATA"))
                                                 .resolve("autorecorder");
    private final File settingsFile;
    
    private static class SettingsContainer
    {
        public String obsPath;
        public String recordingPath;
        
        public String ffmpegPath = Settings.SETTINGS_DIR.resolve("ffmpeg.exe").toString();
        public String ffprobePath = Settings.SETTINGS_DIR.resolve("ffprobe.exe").toString();
        public String clipPath;
        
        public Set<String> excludedGames = new HashSet<>();
        public Set<String> additionalGames = new HashSet<>();
        public Set<String> games = new HashSet<>();
        public long lastFetchedGamesTimestamp;
    }
    
    private ObjectMapper objectMapper;
    private SettingsContainer container;
    private boolean firstLaunch = false;
    
    public Settings()
    {
        objectMapper = new ObjectMapper();
        container = new SettingsContainer();
        settingsFile = new File(SETTINGS_DIR.resolve("settings.json").toString());
    }
    
    public void populate()
    {
        if(!settingsFile.exists())
        {
            firstLaunch = true;
            container.excludedGames.add("leagueclientux.exe");
            StatusQueue.postMessage(new StatusMessage(StatusType.INFO, "Creating settings file on first launch"));
        }
        else
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "Loading settings"));
            try
            {
                container = objectMapper.readValue(settingsFile, SettingsContainer.class);
                StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "Settings loaded"));
            }
            catch(IOException e)
            {
                StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Failed to load settings"));
                container.excludedGames.add("leagueclientux.exe");
            }
        }
        
        if(DateTime.now().isAfter(new DateTime(container.lastFetchedGamesTimestamp).plusDays(1)))
        {
            populateGamesFromApi();
            save();
            StatusQueue.postMessage(new StatusMessage(StatusType.SUCCESS, "Settings saved"));
        }
    }
    
    public void save()
    {
        settingsFile.getParentFile().mkdirs();
        
        try
        {
            container.games.addAll(container.additionalGames);
            container.games.removeAll(container.excludedGames);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, container);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public boolean isFirstLaunch()
    {
        return firstLaunch;
    }
    
    public String getObsPath()
    {
        return container.obsPath;
    }
    
    public void setObsPath(String obsPath)
    {
        container.obsPath = obsPath;
    }
    
    public String getRecordingPath()
    {
        return container.recordingPath;
    }
    
    public void setRecordingPath(String recordingPath)
    {
        container.recordingPath = recordingPath;
    }
    
    public String getFfmpegPath()
    {
        return container.ffmpegPath;
    }
    
    public String getFfprobePath()
    {
        return container.ffprobePath;
    }
    
    public String getClipPath()
    {
        return container.clipPath;
    }
    
    public void setClipPath(String clipPath)
    {
        container.clipPath = clipPath;
    }
    
    public Set<String> getGames()
    {
        synchronized(container.games)
        {
            return container.games;
        }
    }
    
    public Set<String> getExcludedGames()
    {
        return container.excludedGames;
    }
    
    public void setExcludedGames(Set<String> games)
    {
        container.excludedGames = games;
        synchronized(container.games)
        {
            container.games.removeAll(container.excludedGames);
        }
    }
    
    public Set<String> getAdditionalGames()
    {
        return container.additionalGames;
    }
    
    public void setAdditionalGames(Set<String> games)
    {
        container.additionalGames = games;
        synchronized(container.games)
        {
            container.games.addAll(container.additionalGames);
            container.games.removeAll(container.excludedGames); //excluded has priority over additional
        }
    }
    
    public String getSettingsFilePath()
    {
        return settingsFile.getAbsolutePath();
    }
    
    private void populateGamesFromApi()
    {
        try
        {
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = null;
            
            // discord's game list is public and requires no auth
            response = client.execute(new HttpGet("https://discord.com/api/v8/applications/detectable"));
            
            JsonNode root = new ObjectMapper().readTree(response.getEntity().getContent());
            System.out.println("got games from discord API: " + root.size());
            synchronized(container.games)
            {
                for(Iterator<JsonNode> gameIter = root.elements(); gameIter.hasNext(); )
                {
                    JsonNode game = gameIter.next();
                    if(game.has("executables"))
                    {
                        for(Iterator<JsonNode> exeIter = game.get("executables").elements(); exeIter.hasNext(); )
                        {
                            JsonNode exe = exeIter.next();
                            String exeName = exe.get("name").textValue().toLowerCase().replace("/", FileSystems.getDefault().getSeparator());
                            if(!container.excludedGames.contains(exeName))
                            {
                                container.games.add(exeName);
                            }
                        }
                    }
        
                }
                System.out.println("got executables: " + container.games.size());
                container.lastFetchedGamesTimestamp = DateTime.now().getMillis();
            }
        }
        catch(IOException e)
        {
            StatusQueue.postMessage(new StatusMessage(StatusType.WARNING, "Failed to retrieve game list"));
        }
    }
}
