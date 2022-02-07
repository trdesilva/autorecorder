/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.status;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class StatusQueue
{
    private static StatusQueue instance = new StatusQueue();
    
    private static StatusQueue getInstance()
    {
        return instance;
    }
    
    private ConcurrentLinkedQueue<StatusMessage> messageQueue;
    private Semaphore semaphore;
    private StatusConsumer consumer;
    private Thread consumerThread;
    
    private StatusQueue()
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
    }
    
    public static void setConsumer(StatusConsumer consumer)
    {
        if(instance.consumer == null)
        {
            instance.consumer = consumer;
            instance.consumerThread.start();
        }
        else
        {
            throw new IllegalStateException("Can only set StatusQueue's consumer once");
        }
    }
    
    public static void postMessage(StatusMessage message)
    {
        instance.messageQueue.offer(message);
        instance.semaphore.release(1);
    }
}
