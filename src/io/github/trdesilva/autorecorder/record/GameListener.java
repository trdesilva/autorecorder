/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.record;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class GameListener implements AutoCloseable
{
    private final Obs obs;
    private final Settings settings;
    private final EventQueue events;
    private final VideoListHandler recordingListHandler;
    
    private final AtomicBoolean recording;
    private final AtomicReference<String> currentGame;
    
    private Thread thread;
    
    @Inject
    public GameListener(Obs obs, Settings settings, EventQueue events,
                        @Named("RECORDING") VideoListHandler recordingListHandler)
    {
        this.obs = obs;
        this.settings = settings;
        this.events = events;
        this.recordingListHandler = recordingListHandler;
        
        recording = new AtomicBoolean(false);
        currentGame = new AtomicReference<>();
    }
    
    public void start()
    {
        events.postEvent(new Event(EventType.DEBUG, "Starting listener thread"));
        thread = new Thread(() ->
                            {
                                while(true)
                                {
                                    if(!recording.get())
                                    {
                                        ProcessHandle.allProcesses().forEach(ph -> {
                                            // some game exes are identified by more of their path than just filename
                                            if(ph.info().command().isPresent())
                                            {
                                                // TODO memoization
                                                Path command = Paths.get(
                                                        ph.info().command().orElse(""));
                                                for(int i = 1; i <= command.getNameCount(); i++)
                                                {
                                                    String program = command.subpath(
                                                                                    command.getNameCount() - i,
                                                                                    command.getNameCount())
                                                                            .toString();
                                                    if(settings.getGames().contains(settings.formatExeName(program)))
                                                    {
                                                        try
                                                        {
                                                            if(recording.get())
                                                            {
                                                                events.postEvent(new Event(EventType.DEBUG,
                                                                                           "already recording " + currentGame.get()));
                                                                return;
                                                            }
                                                            events.postEvent(
                                                                    new Event(EventType.RECORDING_START,
                                                                              "Recording " + program));
                                                            recordingListHandler.runAutoDelete(); // TODO #7 change this to status consumer
                                                            recording.set(true);
                                                            obs.start();
                                                            currentGame.set(program);
                                                        }
                                                        catch(IOException e)
                                                        {
                                                            // TODO after StatusQueue -> EventQueue refactor, thread should wait for settings update event
                                                            events.postEvent(new Event(EventType.RECORDING_END, e.getMessage()));
                                                            events.postEvent(new Event(EventType.FAILURE,
                                                                                       "Couldn't start OBS"));
                                                            recording.set(false);
                                                            currentGame.set(null);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        if(!settings.getGames().contains(settings.formatExeName(currentGame.get()))
                                        || ProcessHandle.allProcesses()
                                                        .noneMatch(ph -> Paths.get(ph.info().command().orElse(""))
                                                                              .endsWith(currentGame.get())))
                                        {
                                            events.postEvent(new Event(EventType.RECORDING_END,
                                                                       "Stopped recording " + currentGame.get()));
                                            obs.stop();
                                            recording.set(false);
                                            currentGame.set(null);
                                        }
                                    }
                
                                    try
                                    {
                                        Thread.sleep(1000);
                                    }
                                    catch(InterruptedException e)
                                    {
                                        events.postEvent(new Event(EventType.DEBUG, "Game listening ended"));
                                        return;
                                    }
                                }
                            });
        thread.setName("Listener thread");
        thread.start();
    }
    
    public void stop()
    {
        thread.interrupt();
        if(recording.get())
        {
            events.postEvent(new Event(EventType.DEBUG, "thread shutting down, stopping recording"));
            obs.stop();
        }
    }
    
    
    @Override
    public void close() throws Exception
    {
        stop();
    }
}
