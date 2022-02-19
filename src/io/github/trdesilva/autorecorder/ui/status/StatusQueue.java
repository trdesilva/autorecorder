/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.status;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

@Singleton
public class StatusQueue
{
    private final ConcurrentLinkedQueue<StatusMessage> messageQueue;
    private final Semaphore semaphore;
    private StatusConsumer consumer;
    private final Thread consumerThread;
    
    @Inject
    public StatusQueue()
    {
        messageQueue = new ConcurrentLinkedQueue<>();
        messageQueue.offer(new StatusMessage(StatusType.SUCCESS, "Welcome to Autorecorder"));
        semaphore = new Semaphore(1);
        
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
                
                StatusMessage message = messageQueue.poll();
                
                try
                {
                    consumer.post(message);
                }
                catch(InterruptedException e)
                {
                
                }
            }
        });
        consumerThread.setName("Status thread");
    }
    
    public void setConsumer(StatusConsumer consumer)
    {
        if(this.consumer == null && consumer != null)
        {
            this.consumer = consumer;
            consumerThread.start();
        }
        else if(this.consumer != null)
        {
            throw new IllegalStateException("Can only set StatusQueue's consumer once");
        }
    }
    
    public void postMessage(StatusMessage message)
    {
        messageQueue.offer(message);
        semaphore.release(1);
    }
}
