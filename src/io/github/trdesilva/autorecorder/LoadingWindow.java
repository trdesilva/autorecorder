/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Toolkit;

public class LoadingWindow
{
    private final JFrame frame;
    private final Thread updateThread;
    
    public LoadingWindow() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException,
                                  IllegalAccessException
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        frame = new JFrame("Autorecorder Startup");
        
        frame.setIconImage(
                Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("autorecordericon.png")));
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Loading.");
        
        panel.add(label);
        
        updateThread = new Thread(() -> {
            int dots = 0;
            while(true)
            {
                String text = "Loading";
                for(int i = 0; i < dots + 1; i++)
                {
                    text += '.';
                }
                label.setText(text);
                dots = (dots + 1) % 3;
                try
                {
                    Thread.sleep(500);
                }
                catch(InterruptedException e)
                {
                    // no-op
                }
            }
        });
        
        frame.getContentPane().add(panel);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        updateThread.setName("Loading updater");
        updateThread.start();
    }
    
    public void close()
    {
        frame.setVisible(false);
        updateThread.interrupt();
        frame.dispose();
    }
}
