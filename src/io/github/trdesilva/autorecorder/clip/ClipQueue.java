/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

@Singleton
public class ClipQueue implements AutoCloseable
{
    private final ClipTrimmer trimmer;
    private final EventQueue events;
    private final ClipJobValidator validator;
    
    private final ConcurrentLinkedQueue<ClipJob> jobs;
    private final Semaphore semaphore;
    private Thread clippingThread;
    
    @Inject
    public ClipQueue(ClipTrimmer trimmer, EventQueue events, ClipJobValidator validator)
    {
        this.trimmer = trimmer;
        this.events = events;
        this.validator = validator;
        
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
                        if(validator.validate(job))
                        {
                            events.postEvent(new Event(EventType.INFO, "Saving clip: " + job.getDest()));
                            events.postEvent(new Event(EventType.CLIP_START, "Clipping " + job.getDest(), Collections.singletonMap(
                                    EventProperty.CLIP_JOB, job)));
                            if(!job.isSegmented())
                            {
                                trimmer.makeClip(job.getSource(), job.getDest(), job.getStartArgs().get(0), job.getEndArgs().get(0));
                            }
                            else
                            {
                                trimmer.makeSegmentedClip(job.getSource(), job.getDest(), job.getStartArgs(), job.getEndArgs());
                            }
                            events.postEvent(new Event(EventType.SUCCESS, "Clip created: " + job.getDest()));
                            events.postEvent(new Event(EventType.CLIP_END, "Created clip " + job.getDest(), Collections.singletonMap(
                                    EventProperty.CLIP_JOB, job)));
                        }
                    }
                    catch(Exception e)
                    {
                        events.postEvent(
                                new Event(EventType.FAILURE, "Failed to create clip: " + job.getDest()));
                        events.postEvent(new Event(EventType.DEBUG, e.getMessage()));
                        events.postEvent(new Event(EventType.CLIP_END, "", Collections.singletonMap(
                                EventProperty.CLIP_JOB, job)));
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
