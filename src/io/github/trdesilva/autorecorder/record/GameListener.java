/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.record;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GameListener implements AutoCloseable
{
    Obs obs;
    Settings settings;
    AtomicBoolean recording;
    AtomicReference<String> currentGame;
    
    Thread thread;
    
    public GameListener(Obs obs, Settings settings)
    {
        this.obs = obs;
        this.settings = settings;
        
        recording = new AtomicBoolean(false);
        currentGame = new AtomicReference<>();
    }
    
    public void start()
    {
        StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "Starting listener thread"));
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
                                                                      Path command = Paths.get(ph.info().command().orElse(""));
                                                                      for(int i = 1; i <= command.getNameCount(); i++)
                                                                      {
                                                                          String program = command.subpath(command.getNameCount() - i, command.getNameCount())
                                                                                                  .toString();
                                                                          if(settings.getGames().contains(settings.formatExeName(program)))
                                                                          {
                                                                              try
                                                                              {
                                                                                  if(recording.get())
                                                                                  {
                                                                                      StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "already recording " + currentGame.get()));
                                                                                      return;
                                                                                  }
                                                                                  StatusQueue.postMessage(new StatusMessage(StatusType.RECORDING_START, "Recording " + program));
                                                                                  recording.set(true);
                                                                                  obs.start();
                                                                                  currentGame.set(program);
                                                                              }
                                                                              catch(IOException e)
                                                                              {
                                                                                  StatusQueue.postMessage(new StatusMessage(StatusType.FAILURE, "Couldn't start OBS"));
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
                                            StatusQueue.postMessage(new StatusMessage(StatusType.RECORDING_END, "Stopped recording " + currentGame.get()));
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
                                        StatusQueue.postMessage(new StatusMessage(StatusType.DEBUG, "Game listening ended"));
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
            System.out.println("thread shutting down, stopping recording");
            obs.stop();
        }
    }
    
    
    @Override
    public void close() throws Exception
    {
        stop();
    }
}
