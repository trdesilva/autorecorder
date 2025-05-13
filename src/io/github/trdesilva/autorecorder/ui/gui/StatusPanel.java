/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventProperty;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import net.miginfocom.swing.MigLayout;

import javax.inject.Named;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;


public class StatusPanel extends DefaultPanel implements EventConsumer
{
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.SUCCESS,
                                                                            EventType.FAILURE,
                                                                            EventType.WARNING,
                                                                            EventType.INFO,
                                                                            EventType.DEBUG);
    private final EventQueue events;
    private final IndicatorPanel indicatorPanel;
    private final boolean isDebugMode;
    
    private final JLabel messageLabel;
    
    private ConcurrentLinkedQueue<Event> messageQueue;
    private Semaphore semaphore;
    
    private MouseListener mouseListener;
    
    @Inject
    public StatusPanel(EventQueue events, IndicatorPanel indicatorPanel, @Named("isDebugMode") boolean isDebugMode)
    {
        this.events = events;
        this.indicatorPanel = indicatorPanel;
        this.isDebugMode = isDebugMode;
        
        setLayout(new MigLayout("fill", "[grow][]", "[grow]"));
        
        messageLabel = new JLabel("Welcome to Autorecorder");
        
        add(messageLabel, "cell 0 0, growx, growy, dock west, gapleft 4");
        add(this.indicatorPanel, "cell 1 0, growy, dock east");
        
        messageQueue = new ConcurrentLinkedQueue<>();
        semaphore = new Semaphore(0);
        
        events.addConsumer(this);
        Thread updateThread = new Thread(this::consume);
        updateThread.setName("Status panel thread");
        updateThread.start();
    }
    
    @Override
    public synchronized void post(Event event)
    {
        messageQueue.offer(event);
        semaphore.release();
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
    
    private void consume()
    {
        while(true)
        {
            try
            {
                semaphore.acquire();
            }
            catch(InterruptedException e)
            {
                events.postEvent(new Event(EventType.DEBUG, "StatusPanel consumer thread interrupted"));
                return;
            }
            
            Event event = messageQueue.poll();
            boolean showMessage = true;
            if(event.getType() == EventType.DEBUG)
            {
                showMessage = isDebugMode;
            }
            
            if(showMessage)
            {
                Color background;
                switch(event.getType())
                {
                    case FAILURE:
                        background = UIManager.getLookAndFeel().getDefaults().getColor("StatusPanel.failureBackground");
                        break;
                    case SUCCESS:
                        background = UIManager.getLookAndFeel().getDefaults().getColor("StatusPanel.successBackground");
                        break;
                    case WARNING:
                        background = UIManager.getLookAndFeel().getDefaults().getColor("StatusPanel.warningBackground");
                        break;
                    case INFO:
                        background = UIManager.getLookAndFeel().getDefaults().getColor("StatusPanel.infoBackground");
                        break;
                    default:
                        background = UIManager.getLookAndFeel().getDefaults().getColor("StatusPanel.defaultBackground");
                }
                
                SwingUtilities.invokeLater(() -> {
                    setBackground(background);
                    indicatorPanel.setBackground(background);
                    messageLabel.setText(event.getMessage());
                    messageLabel.setToolTipText(event.getTimestamp().toString());
                    
                    String link = (String) (event.getProperties().get(EventProperty.LINK));
                    if(link != null)
                    {
                        mouseListener = new MouseAdapter()
                        {
                            @Override
                            public void mouseClicked(MouseEvent e)
                            {
                                try
                                {
                                    Desktop.getDesktop().browse(new URI(link));
                                }
                                catch(URISyntaxException | IOException ex)
                                {
                                    events.postEvent(new Event(EventType.DEBUG, "Link navigation failed: " + link));
                                }
                            }
                        };
                        messageLabel.addMouseListener(mouseListener);
                        messageLabel.setText(
                                String.format("<html>%s (<a href='%s'>%s</a>)</html>", event.getMessage(), link, link));
                        messageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    else
                    {
                        messageLabel.removeMouseListener(mouseListener);
                        messageLabel.setCursor(Cursor.getDefaultCursor());
                    }
                });
    
                try
                {
                    Thread.sleep(1500);
                }
                catch(InterruptedException e)
                {
                    events.postEvent(new Event(EventType.DEBUG, "status panel thread interrupted"));
                }
            }
        }
    }
}
