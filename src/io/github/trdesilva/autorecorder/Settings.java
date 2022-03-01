/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;
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
import java.util.stream.Collectors;

@Singleton
public class Settings
{
    public static final Path SETTINGS_DIR = Paths.get(System.getenv("LOCALAPPDATA"))
                                                 .resolve("autorecorder");
    private final File settingsFile;
    
    public static class SettingsContainer
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
        
        public boolean termsAccepted = false;
        
        public boolean autoDeleteEnabled = false;
        public int autoDeleteThresholdGB = 100;
    }
    
    private final EventQueue events;
    
    private final ObjectMapper objectMapper;
    private SettingsContainer container;
    
    @Inject
    public Settings(EventQueue events)
    {
        this.events = events;
        
        objectMapper = new ObjectMapper();
        container = new SettingsContainer();
        settingsFile = new File(SETTINGS_DIR.resolve("settings.json").toString());
    }
    
    public void populate()
    {
        if(!settingsFile.exists())
        {
            container.excludedGames.add("leagueclientux.exe");
            events.postEvent(new Event(EventType.INFO, "Creating settings file on first launch"));
        }
        else
        {
            events.postEvent(new Event(EventType.DEBUG, "Loading settings"));
            try
            {
                container = objectMapper.readValue(settingsFile, SettingsContainer.class);
                events.postEvent(new Event(EventType.DEBUG, "Settings loaded"));
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Failed to load settings"));
                container.excludedGames.add("leagueclientux.exe");
            }
        }
        
        if(DateTime.now().isAfter(new DateTime(container.lastFetchedGamesTimestamp).plusDays(1)))
        {
            populateGamesFromApi();
            save();
            events.postEvent(new Event(EventType.SUCCESS, "Settings saved"));
        }
    }
    
    public void save()
    {
        if(!settingsFile.getParentFile().exists())
        {
            settingsFile.getParentFile().mkdirs();
            settingsFile.getParentFile().setReadable(true);
            settingsFile.getParentFile().setWritable(true);
        }
        
        try
        {
            container.games.addAll(container.additionalGames);
            container.games.removeAll(container.excludedGames);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, container);
            events.postEvent(new Event(EventType.SETTINGS_CHANGE, "Settings saved"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public boolean isTermsAccepted()
    {
        return container.termsAccepted;
    }
    
    public void setTermsAccepted(boolean termsAccepted)
    {
        container.termsAccepted = termsAccepted;
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
        container.excludedGames = games.stream().map(this::formatExeName).collect(Collectors.toSet());
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
        Set<String> removed = Sets.difference(getAdditionalGames(), games)
                                  .stream()
                                  .map(this::formatExeName)
                                  .collect(Collectors.toSet());
        container.additionalGames = games.stream().map(this::formatExeName).collect(Collectors.toSet());
        synchronized(container.games)
        {
            container.games.removeAll(removed);
            container.games.addAll(container.additionalGames);
            container.games.removeAll(container.excludedGames); //excluded has priority over additional
        }
    }
    
    public boolean isAutoDeleteEnabled()
    {
        return container.autoDeleteEnabled;
    }
    
    public void setAutoDeleteEnabled(boolean autoDeleteEnabled)
    {
        this.container.autoDeleteEnabled = autoDeleteEnabled;
    }
    
    public int getAutoDeleteThresholdGB()
    {
        return container.autoDeleteThresholdGB;
    }
    
    public void setAutoDeleteThresholdGB(int autoDeleteThresholdGB)
    {
        this.container.autoDeleteThresholdGB = autoDeleteThresholdGB;
    }
    
    public String getSettingsFilePath()
    {
        return settingsFile.getAbsolutePath();
    }
    
    public String formatExeName(String original)
    {
        return original.toLowerCase().replace("/", FileSystems.getDefault().getSeparator());
    }
    
    private void populateGamesFromApi()
    {
        try
        {
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = null;
            
            // discord's game list is public and requires no auth
            response = client.execute(new HttpGet("https://discord.com/api/v10/applications/detectable"));
            
            JsonNode root = new ObjectMapper().readTree(response.getEntity().getContent());
            events.postEvent(new Event(EventType.DEBUG, "got games from discord API: " + root.size()));
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
                            String exeName = formatExeName(exe.get("name").textValue());
                            if(!container.excludedGames.contains(exeName))
                            {
                                container.games.add(exeName);
                            }
                        }
                    }
                    
                }
                events.postEvent(new Event(EventType.DEBUG,"got executables: " + container.games.size()));
                container.lastFetchedGamesTimestamp = DateTime.now().getMillis();
            }
        }
        catch(IOException e)
        {
            events.postEvent(new Event(EventType.WARNING, "Failed to retrieve game list"));
        }
    }
}
