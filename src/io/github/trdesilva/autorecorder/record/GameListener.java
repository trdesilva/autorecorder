package io.github.trdesilva.autorecorder.record;

import io.github.trdesilva.autorecorder.Settings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GameListener
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
        System.out.println("starting listener thread");
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
                                                                                                  .toString()
                                                                                                  .toLowerCase();
                                                                          if(settings.getGames().contains(program))
                                                                          {
                                                                              try
                                                                              {
                                                                                  System.out.println("game started: " + program);
                                                                                  if(recording.get())
                                                                                  {
                                                                                      System.out.println("already recording " + currentGame.get());
                                                                                      return;
                                                                                  }
                                                                                  recording.set(true);
                                                                                  obs.start();
                                                                                  currentGame.set(program);
                                                                              }
                                                                              catch(IOException e)
                                                                              {
                                                                                  System.out.println("couldn't start obs");
                                                                              }
                                                                              break;
                                                                          }
                                                                      }
                                                                  }
                                                              });
                                    }
                                    else
                                    {
                                        if(ProcessHandle.allProcesses()
                                                        .noneMatch(ph -> Paths.get(ph.info()
                                                                                     .command()
                                                                                     .orElse(""))
                                                                              .endsWith(currentGame.get())
                                                                  ))
                                        {
                                            System.out.println("game ended: " + currentGame.get());
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
                                        System.out.println("listening ended");
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
    
    
}
