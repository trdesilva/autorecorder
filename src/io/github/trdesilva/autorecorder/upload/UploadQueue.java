/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload;

import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.ui.status.StatusType;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class UploadQueue
{
    private Uploader uploader;
    
    private ConcurrentLinkedQueue<UploadJob> jobs;
    private Semaphore semaphore;
    private Thread uploadingThread;
    
    public UploadQueue(Uploader uploader)
    {
        this.uploader = uploader;
        
        jobs = new ConcurrentLinkedQueue<>();
        semaphore = new Semaphore(0);
    }
    
    public synchronized void enqueue(UploadJob clipJob)
    {
        jobs.offer(clipJob);
        semaphore.release(1);
    }
    
    public void startProcessing()
    {
        if(uploader != null && (uploadingThread == null || !uploadingThread.isAlive()))
        {
            uploadingThread = new Thread(() -> {
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
                    UploadJob job = jobs.poll();
                    try
                    {
                        if(uploader.getValidator().validate(job))
                        {
                            StatusQueue.postMessage(new StatusMessage(StatusType.INFO, "Starting upload of " + job.getVideoTitle()));
                            String url = uploader.upload(job);
                            StatusQueue.postMessage(new StatusMessage(StatusType.SUCCESS, job.getVideoTitle() + " uploaded", url));
                        }
                    }
                    catch(Exception e)
                    {
                        StatusQueue.postMessage(new StatusMessage(StatusType.FAILURE, "Failed to upload clip: " + job.getClipName()));
                    }
                }
            });
            uploadingThread.setName("Uploading thread");
            uploadingThread.start();
        }
    }
    
    public void stopProcessing()
    {
        if(uploadingThread != null && uploadingThread.isAlive())
        {
            uploadingThread.interrupt();
        }
    }
}
