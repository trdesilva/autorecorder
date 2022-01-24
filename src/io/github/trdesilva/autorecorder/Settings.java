package io.github.trdesilva.autorecorder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Settings
{
    
    private final File settingsFile;
    
    private static class SettingsContainer
    {
        public String obsPath;
        public String recordingPath;
        
        public String ffmpegPath;
        public String clipPath;
        
        public Set<String> excludedGames = new HashSet<>();
        public Set<String> games = new HashSet<>();
        public long lastFetchedGamesTimestamp;
    }
    
    private ObjectMapper objectMapper;
    private SettingsContainer container;
    
    public Settings()
    {
        objectMapper = new ObjectMapper();
        container = new SettingsContainer();
        settingsFile = new File(Paths.get(System.getenv("LOCALAPPDATA"))
                                     .resolve("autorecorder")
                                     .resolve("settings.json")
                                     .toString());
    }
    
    public void populate()
    {
        try
        {
            container = objectMapper.readValue(settingsFile, SettingsContainer.class);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            container.obsPath = "F:\\Program Files\\obs-studio\\bin\\64bit\\obs64.exe";
            container.excludedGames.add("leagueclientux.exe");
        }
        
        if(DateTime.now().isAfter(new DateTime(container.lastFetchedGamesTimestamp).plusDays(1)))
        {
            populateGamesFromApi();
            save();
        }
    }
    
    public void save()
    {
        settingsFile.getParentFile().mkdirs();
        
        try
        {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, container);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public String getObsPath()
    {
        return container.obsPath;
    }
    
    public String getRecordingPath()
    {
        return container.recordingPath;
    }
    
    public String getClipPath()
    {
        return container.clipPath;
    }
    
    public Set<String> getGames()
    {
        return container.games;
    }
    
    public Set<String> getExcludedGames()
    {
        return container.excludedGames;
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
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
