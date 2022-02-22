/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.record;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

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
    private final StatusQueue status;
    
    private final AtomicBoolean recording;
    private final AtomicReference<String> currentGame;
    
    private Thread thread;
    
    @Inject
    public GameListener(Obs obs, Settings settings, StatusQueue status)
    {
        this.obs = obs;
        this.settings = settings;
        this.status = status;
        
        recording = new AtomicBoolean(false);
        currentGame = new AtomicReference<>();
    }
    
    public void start()
    {
        status.postMessage(new StatusMessage(StatusType.DEBUG, "Starting listener thread"));
        thread = new Thread(() ->
                            {
                                while(true)
                                {
                                    if(!recording.get())
                                    {
                                        ProcessHandle.allProcesses()
                                                     .forEach(ph ->
                                                              {
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
                                                                          if(settings.getGames()
                                                                                     .contains(settings.formatExeName(
                                                                                             program)))
                                                                          {
                                                                              try
                                                                              {
                                                                                  if(recording.get())
                                                                                  {
                                                                                      status.postMessage(
                                                                                              new StatusMessage(
                                                                                                      StatusType.DEBUG,
                                                                                                      "already recording " + currentGame.get()));
                                                                                      return;
                                                                                  }
                                                                                  status.postMessage(new StatusMessage(
                                                                                          StatusType.RECORDING_START,
                                                                                          "Recording " + program));
                                                                                  recording.set(true);
                                                                                  obs.start();
                                                                                  currentGame.set(program);
                                                                              }
                                                                              catch(IOException e)
                                                                              {
                                                                                  status.postMessage(new StatusMessage(
                                                                                          StatusType.FAILURE,
                                                                                          "Couldn't start OBS"));
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
                                                                .noneMatch(ph -> Paths.get(ph.info()
                                                                                             .command()
                                                                                             .orElse(""))
                                                                                      .endsWith(currentGame.get())
                                                                          ))
                                        {
                                            status.postMessage(new StatusMessage(StatusType.RECORDING_END,
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
                                        status.postMessage(new StatusMessage(StatusType.DEBUG, "Game listening ended"));
                                        return;
                                    }
                                }
                            });
        thread.start();
    }
    
    public void stop()
    {
        thread.interrupt();
        if(recording.get())
        {
            status.postMessage(new StatusMessage(StatusType.DEBUG,"thread shutting down, stopping recording"));
            obs.stop();
        }
    }
    
    
    @Override
    public void close() throws Exception
    {
        stop();
    }
}
