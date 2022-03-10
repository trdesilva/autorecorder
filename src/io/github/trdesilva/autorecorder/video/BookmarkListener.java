/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.TimestampUtil;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import org.joda.time.DateTime;

import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class BookmarkListener implements EventConsumer, NativeKeyListener, AutoCloseable
{
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.RECORDING_START,
                                                                            EventType.RECORDING_END);
    
    
    private final VideoListHandler recordingHandler;
    private final Settings settings;
    private final EventQueue events;
    
    private final AtomicLong recordingStart;
    
    @Inject
    public BookmarkListener(@Named("RECORDING") VideoListHandler recordingHandler, Settings settings, EventQueue events)
    {
        this.recordingHandler = recordingHandler;
        this.settings = settings;
        this.events = events;
        
        recordingStart = new AtomicLong(0);
        
        events.addConsumer(this);
    }
    
    @Override
    public void post(Event event)
    {
        if(event.getType().equals(EventType.RECORDING_START) && settings.areBookmarksEnabled())
        {
            try
            {
                GlobalScreen.registerNativeHook();
                GlobalScreen.addNativeKeyListener(this);
                recordingStart.set(DateTime.now().getMillis());
                events.postEvent(new Event(EventType.DEBUG, "started bookmark listening"));
            }
            catch(NativeHookException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Failed to start listening for bookmark key"));
            }
        }
        else if(event.getType().equals(EventType.RECORDING_END))
        {
            try
            {
                GlobalScreen.removeNativeKeyListener(this);
                GlobalScreen.unregisterNativeHook();
                events.postEvent(new Event(EventType.DEBUG, "stopped bookmark listening"));
            }
            catch(NativeHookException e)
            {
                events.postEvent(new Event(EventType.WARNING, "Failed to stop listening for bookmark key"));
            }
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent)
    {
        if(settings.getBookmarkKey().eventMatches(nativeEvent))
        {
            File recording = recordingHandler.getVideoList()
                                             .stream()
                                             .max(Comparator.comparing(File::lastModified).reversed())
                                             .get();
            VideoMetadata metadata = recordingHandler.getMetadata(recording);
            long bookmarkTimestamp = DateTime.now().getMillis() - recordingStart.get();
            metadata.getBookmarks().add(bookmarkTimestamp);
            recordingHandler.saveMetadata(recording, metadata);
            events.postEvent(new Event(EventType.INFO, "Saved bookmark at " + TimestampUtil.formatTime(bookmarkTimestamp)));
        }
    }
    
    @Override
    public void close() throws Exception
    {
        GlobalScreen.unregisterNativeHook();
        events.postEvent(new Event(EventType.DEBUG, "unregistered bookmark listener"));
    }
}
