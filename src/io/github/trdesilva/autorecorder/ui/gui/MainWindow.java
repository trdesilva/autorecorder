package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MainWindow
{
    public static final int PREFERRED_WIDTH = 800;
    public static final int PREFERRED_HEIGHT = 600;
    
    public static Set<AutoCloseable> closeables = new HashSet<>();
    
    private static MainWindow instance;
    private final JPanel mainPanel;
    private final CardLayout mainLayout;
    
    public static MainWindow getInstance()
    {
        return instance;
    }
    
    private final JFrame mainFrame;
    
    private final ClippingPanel clippingPanel;
    
    public MainWindow(Settings settings) throws Exception
    {
        instance = this;
        
        // components
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        mainFrame = new JFrame("Autorecorder");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        mainPanel = new JPanel();
    
        mainLayout = new CardLayout();
        mainPanel.setLayout(mainLayout);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Recordings", new RecordingListPanel(settings));
        tabbedPane.add("Clips", new ClipListPanel(settings));
        tabbedPane.add("Settings", new SettingsPanel());
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        
        clippingPanel = new ClippingPanel();
        
        mainPanel.add(tabbedPane, "mainView");
        mainPanel.add(clippingPanel, "clippingView");
        mainLayout.show(mainPanel, "mainView");
        
        mainFrame.getContentPane().add(mainPanel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        // TODO add status bar?
        
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
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
            }
        });
    }
    
    public JPanel getMainPanel()
    {
        return mainPanel;
    }
    
    public void showClipView(File videoFile)
    {
        System.out.println("showing clippingView");
        mainLayout.show(mainPanel, "clippingView");
        clippingPanel.setRecording(videoFile);
    }
    
    public void showMainView()
    {
        mainLayout.show(mainPanel, "mainView");
    }
}
