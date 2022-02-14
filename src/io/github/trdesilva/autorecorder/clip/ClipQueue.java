/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class ClipQueue implements AutoCloseable
{
    private final ClipTrimmer trimmer;
    private final StatusQueue status;
    
    private final ConcurrentLinkedQueue<ClipJob> jobs;
    private final Semaphore semaphore;
    private Thread clippingThread;
    
    @Inject
    public ClipQueue(ClipTrimmer trimmer, StatusQueue status)
    {
        this.trimmer = trimmer;
        this.status = status;
        
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
                        status.postMessage(new StatusMessage(StatusType.SUCCESS, "Clip created: " + job.getDest()));
                    }
                    catch(IOException | InterruptedException e)
                    {
                        status.postMessage(
                                new StatusMessage(StatusType.FAILURE, "Failed to create clip: " + job.getDest()));
                    }
                }
            });
            clippingThread.setName("Clipping thread");
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
    
    @Override
    public void close() throws Exception
    {
        stopProcessing();
    }
}
