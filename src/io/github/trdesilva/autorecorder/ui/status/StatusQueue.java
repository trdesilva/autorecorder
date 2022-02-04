package io.github.trdesilva.autorecorder.ui.status;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class StatusQueue
{
    private static StatusQueue instance = new StatusQueue();
    
    public static StatusQueue getInstance()
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
                consumer.post(message);
            }
        });
    }
    
    public void setConsumer(StatusConsumer consumer)
    {
        this.consumer = consumer;
        consumerThread.start();
    }
    
    public void postMessage(StatusMessage message)
    {
        messageQueue.offer(message);
        semaphore.release(1);
    }
}
