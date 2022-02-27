/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;

public class Updater
{
    public static final Path SETTINGS_DIR = Paths.get(System.getenv("LOCALAPPDATA"))
                                                 .resolve("autorecorder");
    public static final String LAUNCHER_FILENAME = "launcher.cmd";
    public static final String GITHUB_RELEASE_LATEST_URL = "https://api.github.com/repos/trdesilva/autorecorder/releases/latest";
    public static final String ASSETS_FIELD = "assets";
    public static final String ASSET_NAME_FIELD = "name";
    public static final String ASSET_URL_FIELD = "url";
    
    public static void main(String[] args) throws Exception
    {
        File installDir = SETTINGS_DIR.toFile();
        if(!installDir.exists())
        {
            System.out.println("Settings directory missing; performing first time setup (make sure to get the YouTube API client secrets file)...");
            if(!installDir.mkdirs())
            {
                System.err.println("Failed to create install directory");
                throw new IOException("Failed to create install directory at " + installDir.getAbsolutePath());
            }
            installDir.setReadable(true);
            installDir.setWritable(true);
        }
        
        File[] currentFiles = installDir.listFiles();
        if(Arrays.stream(currentFiles).noneMatch(file -> file.getName().equals(LAUNCHER_FILENAME)))
        {
            System.out.println("Creating launcher script...");
            Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream(LAUNCHER_FILENAME),
                       SETTINGS_DIR.resolve(LAUNCHER_FILENAME));
            File launcher = SETTINGS_DIR.resolve(LAUNCHER_FILENAME).toFile();
            launcher.setReadable(true, true);
            launcher.setWritable(true, true);
            launcher.setExecutable(true, true);
            System.out.println("Created launcher script at " + SETTINGS_DIR.resolve(
                    LAUNCHER_FILENAME) + " (make Windows run this on startup if you want Autorecorder to launch on boot)");
            System.out.println("Please run the launcher script to continue");
            return;
        }
    
        System.out.println("Checking current version...");
        Optional<String> jarName = Arrays.stream(currentFiles)
                                         .map(File::getName)
                                         .filter(Regex.JAR_PATTERN.asMatchPredicate())
                                         .max(Comparator.comparing(VersionNumber::fromJarName));
        VersionNumber jarVersion;
        if(jarName.isPresent())
        {
            jarVersion = VersionNumber.fromJarName(jarName.get());
            System.out.println("Found version " + jarVersion);
        }
        else
        {
            jarVersion = new VersionNumber(0, 0, 0);
            System.out.println("No versioned JAR found");
        }
        
        System.out.println("Checking latest version available...");
        ObjectMapper objectMapper = new ObjectMapper();
        try(CloseableHttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build())
        {
            CloseableHttpResponse latestResponse = httpClient.execute(new HttpGet(GITHUB_RELEASE_LATEST_URL));
            if(latestResponse.getStatusLine().getStatusCode() == 200)
            {
                JsonNode responseJson = objectMapper.readTree(latestResponse.getEntity().getContent());
                VersionNumber latestVersion = VersionNumber.fromTag(responseJson.get("tag_name").asText());
                System.out.println("Latest version available is " + latestVersion);
                if(latestVersion.compareTo(jarVersion) > 0)
                {
                    System.out.println("Downloading version " + latestVersion + "...");
                    for(Iterator<JsonNode> it = responseJson.get(ASSETS_FIELD).elements(); it.hasNext(); )
                    {
                        JsonNode asset = it.next();
        
                        String assetName = asset.get(ASSET_NAME_FIELD).asText();
                        if(Regex.JAR_PATTERN.matcher(assetName).matches())
                        {
                            HttpGet downloadRequest = new HttpGet(asset.get(ASSET_URL_FIELD).asText());
                            downloadRequest.addHeader("Accept", ContentType.APPLICATION_OCTET_STREAM.getMimeType());
                            CloseableHttpResponse downloadResponse = httpClient.execute(downloadRequest);
                            if(downloadResponse.getStatusLine().getStatusCode() == 200)
                            {
                                File updatedJarFile = SETTINGS_DIR.resolve(assetName).toFile();
                                FileOutputStream updatedJarFileStream = new FileOutputStream(updatedJarFile);
                                IOUtils.copy(downloadResponse.getEntity().getContent(), updatedJarFileStream);
                                updatedJarFile.setReadable(true);
                                updatedJarFile.setWritable(true);
                                updatedJarFile.setExecutable(true);
                                updatedJarFileStream.close();
                                System.out.println("Download complete; updated to version " + latestVersion);
    
                                jarName.ifPresent(s -> new File(s).delete());
                            }
                            else
                            {
                                System.err.println("Failed to download latest version from GitHub");
                                throw new IOException("GitHub request failed with status " + downloadResponse.getStatusLine());
                            }
                            
                            break;
                        }
                    }
                }
                else
                {
                    System.out.println("Already up-to-date");
                }
            }
            else
            {
                System.err.println("Failed to retrieve version metadata from GitHub");
                throw new IOException("GitHub request failed with status " + latestResponse.getStatusLine());
            }
        }
    }
}
