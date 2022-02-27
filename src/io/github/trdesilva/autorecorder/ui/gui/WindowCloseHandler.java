/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class WindowCloseHandler extends WindowAdapter
{
    private final Set<AutoCloseable> closeables = new HashSet<>();
    
    @Inject
    public WindowCloseHandler()
    {
    
    }
    
    public void addCloseable(AutoCloseable closeable)
    {
        closeables.add(closeable);
    }
    
    @Override
    public void windowClosing(WindowEvent e)
    {
        super.windowClosing(e);
        System.out.println("close started");
        closeables.forEach(ac -> {
            try
            {
                ac.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        });
        System.out.println("close complete");
    }
}
