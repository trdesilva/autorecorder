/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
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

import java.lang.reflect.Field;
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
    
        GlobalScreen.setEventDispatcher(new SwingDispatchService());
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
        else if(event.getType().equals(EventType.RECORDING_END) || (event.getType().equals(EventType.SETTINGS_CHANGE) && !settings.areBookmarksEnabled()))
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
            long bookmarkTimestamp = DateTime.now().getMillis() - recordingStart.get();
            recordingHandler.saveBookmark(bookmarkTimestamp);
            events.postEvent(
                    new Event(EventType.INFO, "Saved bookmark at " + TimestampUtil.formatTime(bookmarkTimestamp)));
        }
        // VC_META is the Windows key
        else if(settings.isConsumeWindowsKeyEnabled() && nativeEvent.getKeyCode() == NativeKeyEvent.VC_META)
        {
            // taken from jnativehook documentation: https://github.com/kwhat/jnativehook/blob/2.2/doc/ConsumingEvents.md
            try
            {
                Field f = NativeInputEvent.class.getDeclaredField("reserved");
                f.setAccessible(true);
                f.setShort(nativeEvent, (short) 0x01);
                events.postEvent(new Event(EventType.DEBUG, "Consumed Windows key"));
            }
            catch(NoSuchFieldException|IllegalAccessException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "failed to consume Windows key: " + e.getMessage()));
            }
        }
    }
    
    @Override
    public void close() throws Exception
    {
        GlobalScreen.unregisterNativeHook();
        events.postEvent(new Event(EventType.DEBUG, "unregistered bookmark listener"));
    }
}
