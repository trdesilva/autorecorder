/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.inject;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LocalAppDataNativeDiscoveryStrategy implements NativeDiscoveryStrategy
{
    private static final Path VLC_DIR = Settings.SETTINGS_DIR.resolve("vlc");
    
    private final EventQueue events;
    
    @Inject
    public LocalAppDataNativeDiscoveryStrategy(EventQueue events)
    {
        this.events = events;
    }
    
    @Override
    public boolean supported()
    {
        return true;
    }
    
    @Override
    public String discover()
    {
        return VLC_DIR.toString();
    }
    
    @Override
    public boolean onFound(String path)
    {
        if(!VLC_DIR.toFile().exists() && !VLC_DIR.toFile().mkdir())
        {
            return false;
        }
        
        if(!VLC_DIR.resolve("libvlc.dll").toFile().exists())
        {
            events.postEvent(new Event(EventType.DEBUG, "copying libvlc.dll from resources"));
            try
            {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("libvlc.dll"),
                           VLC_DIR.resolve("libvlc.dll"));
                File libvlc = VLC_DIR.resolve("libvlc.dll").toFile();
                libvlc.setReadable(true);
                libvlc.setWritable(true);
                libvlc.setExecutable(true);
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "failed to copy libvlc.dll"));
                return false;
            }
        }
        if(!VLC_DIR.resolve("libvlccore.dll").toFile().exists())
        {
            events.postEvent(new Event(EventType.DEBUG, "copying libvlccore.dll from resources"));
            try
            {
                Files.copy(ClassLoader.getSystemClassLoader().getResourceAsStream("libvlccore.dll"),
                           VLC_DIR.resolve("libvlccore.dll"));
                File libvlccore = VLC_DIR.resolve("libvlccore.dll").toFile();
                libvlccore.setReadable(true);
                libvlccore.setWritable(true);
                libvlccore.setExecutable(true);
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "failed to copy libvlccore.dll"));
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean onSetPluginPath(String path)
    {
        File pluginsDir = VLC_DIR.resolve("plugins").toFile();
        if(!pluginsDir.exists())
        {
            try
            {
                ZipInputStream pluginsZip = new ZipInputStream(
                        ClassLoader.getSystemClassLoader().getResourceAsStream("plugins.zip"));
                
                ZipEntry zipEntry = pluginsZip.getNextEntry();
                while(zipEntry != null)
                {
                    File newFile = newFile(VLC_DIR.toFile(), zipEntry);
                    if(zipEntry.isDirectory())
                    {
                        if(!newFile.isDirectory() && !newFile.mkdirs())
                        {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    }
                    else
                    {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if(!parent.isDirectory() && !parent.mkdirs())
                        {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        parent.setReadable(true);
                        parent.setWritable(true);
                        
                        // write file content
                        Files.copy(pluginsZip, newFile.toPath());
                        newFile.setReadable(true);
                    }
                    zipEntry = pluginsZip.getNextEntry();
                }
            }
            catch(IOException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "failed to copy vlc plugins dir"));
                return false;
            }
        }
        System.setProperty("VLC_PLUGIN_PATH", VLC_DIR.resolve("plugins").toString());
        return true;
    }
    
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException
    {
        File destFile = new File(destinationDir, zipEntry.getName());
        
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        
        if(!destFilePath.startsWith(destDirPath + File.separator))
        {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        
        return destFile;
    }
}
