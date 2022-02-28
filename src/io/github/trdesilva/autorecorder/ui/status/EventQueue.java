/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.status;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class EventQueue
{
    private final ConcurrentLinkedQueue<Event> postQueue;
    private final Semaphore semaphore;
    private final List<EventConsumer> consumers;
    private final ExecutorService threadPoolExecutor;
    private final Thread consumerThread;
    
    @Inject
    public EventQueue()
    {
        postQueue = new ConcurrentLinkedQueue<>();
        postQueue.offer(new Event(EventType.SUCCESS, "Welcome to Autorecorder"));
        semaphore = new Semaphore(1);
        consumers = new LinkedList<>();
    
        threadPoolExecutor = Executors.newFixedThreadPool(2);
        
        consumerThread = new Thread(() -> {
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
                
                Event event = postQueue.poll();
                
                for(EventConsumer consumer: consumers)
                {
                    if(consumer.getSubscriptions().contains(event.getType()))
                    {
                        threadPoolExecutor.submit(() -> consumer.post(event));
                    }
                }
            }
        });
        consumerThread.setName("Status thread");
    }
    
    public void addConsumer(EventConsumer consumer)
    {
        if(consumer != null)
        {
            consumers.add(consumer);
            if(!consumerThread.isAlive())
            {
                consumerThread.start();
            }
        }
    }
    
    public void postEvent(Event event)
    {
        postQueue.offer(event);
        semaphore.release(1);
        System.out.println(event);
    }
}
