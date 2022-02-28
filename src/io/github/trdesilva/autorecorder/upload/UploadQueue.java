/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.ui.gui.ReportableException;
import io.github.trdesilva.autorecorder.ui.status.Event;
import io.github.trdesilva.autorecorder.ui.status.EventProperty;
import io.github.trdesilva.autorecorder.ui.status.EventQueue;
import io.github.trdesilva.autorecorder.ui.status.EventType;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

@Singleton
public class UploadQueue implements AutoCloseable
{
    private final Uploader uploader;
    private final EventQueue events;
    
    private final ConcurrentLinkedQueue<UploadJob> jobs;
    private final Semaphore semaphore;
    private Thread uploadingThread;
    
    @Inject
    public UploadQueue(Uploader uploader, EventQueue events)
    {
        this.uploader = uploader;
        this.events = events;
        
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
                            events.postEvent(
                                    new Event(EventType.INFO, "Starting upload of " + job.getVideoTitle()));
                            String url = uploader.upload(job);
                            events.postEvent(
                                    new Event(EventType.SUCCESS, job.getVideoTitle() + " uploaded",
                                              Collections.singletonMap(EventProperty.LINK, url)));
                        }
                    }
                    catch(ReportableException e)
                    {
                        events.postEvent(new Event(EventType.FAILURE,
                                                   String.format("Failed to upload '%s': %s",
                                                                           job.getClipName(), e.getMessage())));
                        events.postEvent(new Event(EventType.DEBUG, Arrays.toString(e.getCause().getStackTrace())));
                    }
                    catch(Exception e)
                    {
                        events.postEvent(new Event(EventType.DEBUG, Arrays.toString(e.getStackTrace())));
                        events.postEvent(new Event(EventType.FAILURE, String.format("Failed to upload '%s'",
                                                                                    job.getClipName())));
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
    
    @Override
    public void close() throws Exception
    {
        stopProcessing();
    }
}
