/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.record;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventConsumer;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class GameListener implements AutoCloseable, EventConsumer
{
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.SETTINGS_CHANGE);
    private final Obs obs;
    private final Settings settings;
    private final EventQueue events;
    
    private final ConcurrentHashMap<Path, Optional<String>> exeCheckResults;
    private final AtomicBoolean recording;
    private final AtomicReference<String> currentGame;
    
    private Thread thread;
    
    @Inject
    public GameListener(Obs obs, Settings settings, EventQueue events)
    {
        this.obs = obs;
        this.settings = settings;
        this.events = events;
        
        exeCheckResults = new ConcurrentHashMap<>();
        recording = new AtomicBoolean(false);
        currentGame = new AtomicReference<>();
        
        events.addConsumer(this);
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
                                                Path command = Paths.get(ph.info().command().orElse(""));
                                                if(!exeCheckResults.containsKey(command))
                                                {
                                                    Optional<String> programOptional = Optional.empty();
                                                    for(int i = 1; i <= command.getNameCount(); i++)
                                                    {
                                                        String program = command.subpath(
                                                                                        command.getNameCount() - i,
                                                                                        command.getNameCount())
                                                                                .toString();
                                                        if(settings.getGames()
                                                                   .contains(settings.formatExeName(program)))
                                                        {
                                                            programOptional = Optional.of(program);
                                                            break;
                                                        }
                                                    }
                                
                                                    exeCheckResults.put(command, programOptional);
                                                }
                            
                                                if(exeCheckResults.containsKey(command) && exeCheckResults.get(command)
                                                                                                          .isPresent())
                                                {
                                                    try
                                                    {
                                                        if(recording.get())
                                                        {
                                                            events.postEvent(new Event(EventType.DEBUG,
                                                                                       "already recording " + currentGame.get()));
                                                            return;
                                                        }
                                                        String program = exeCheckResults.get(command).get();
                                                        recording.set(true);
                                                        obs.start();
                                                        currentGame.set(program);
                                                        events.postEvent(new Event(EventType.RECORDING_START,
                                                                                   "Recording " + program));
                                                    }
                                                    catch(IOException e)
                                                    {
                                                        events.postEvent(new Event(EventType.FAILURE,
                                                                                   "Couldn't start OBS; waiting for settings change before attempting to record again"));
                                                        recording.set(false);
                                                        currentGame.set(null);
                                                        stop();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        if(!settings.getGames().contains(settings.formatExeName(currentGame.get()))
                                                || ProcessHandle.allProcesses()
                                                                .noneMatch(
                                                                        ph -> Paths.get(ph.info().command().orElse(""))
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
    
    @Override
    public void post(Event message)
    {
        if(message.getType().equals(EventType.SETTINGS_CHANGE))
        {
            exeCheckResults.clear();
            if(!thread.isAlive())
            {
                start();
            }
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
}
