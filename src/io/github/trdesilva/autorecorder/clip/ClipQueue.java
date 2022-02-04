package io.github.trdesilva.autorecorder.clip;

import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class ClipQueue
{
    private ClipTrimmer trimmer;
    
    private ConcurrentLinkedQueue<ClipJob> jobs;
    private Semaphore semaphore;
    private Thread clippingThread;
    
    public ClipQueue(ClipTrimmer trimmer)
    {
        this.trimmer = trimmer;
        
        jobs = new ConcurrentLinkedQueue<>();
        semaphore = new Semaphore(0);
    }
    
    public synchronized void enqueue(ClipJob clipJob)
    {
        jobs.offer(clipJob);
        semaphore.release(1);
    }
    
    public void startProcessing()
    {
        if(clippingThread == null || !clippingThread.isAlive())
        {
            clippingThread = new Thread(() -> {
                while(true)
                {
                    try
                    {
                        semaphore.acquire();
                    }
                    catch(InterruptedException e)
                    {
                        return;
                    }
                    ClipJob job = jobs.poll();
                    try
                    {
                        trimmer.makeClip(job.getSource(), job.getDest(), job.getStartArg(), job.getEndArg());
                        StatusQueue.getInstance().postMessage(new StatusMessage(StatusType.SUCCESS, "Clip created: " + job.getDest()));
                    }
                    catch(IOException|InterruptedException e)
                    {
                        StatusQueue.getInstance().postMessage(new StatusMessage(StatusType.FAILURE, "Failed to create clip: " + job.getDest()));
                    }
                }
            });
            clippingThread.start();
        }
    }
    
    public void stopProcessing()
    {
        if(clippingThread != null && clippingThread.isAlive())
        {
            clippingThread.interrupt();
        }
    }
}
