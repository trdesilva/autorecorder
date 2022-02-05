/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.clip.ClipQueue;
import io.github.trdesilva.autorecorder.clip.ClipTrimmer;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
import io.github.trdesilva.autorecorder.upload.UploadQueue;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MainWindow
{
    public static final int PREFERRED_WIDTH = 800;
    public static final int PREFERRED_HEIGHT = 600;
    
    public static Set<AutoCloseable> closeables = new HashSet<>();
    private static MainWindow instance;
    
    public static MainWindow getInstance()
    {
        return instance;
    }
    
    private final JPanel mainPanel;
    private final CardLayout mainLayout;
    private final JFrame mainFrame;
    
    private final RecordingListPanel recordingListPanel;
    private final ClipListPanel clipListPanel;
    
    private final ClippingPanel clippingPanel;
    private final UploadPanel uploadPanel;
    private final StatusPanel statusPanel;
    
    private ClipQueue clipQueue;
    private UploadQueue uploadQueue;
    
    public MainWindow(Settings settings) throws Exception
    {
        instance = this;
        
        // components
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        mainFrame = new JFrame("Autorecorder");
        // TODO figure out minimizing to tray
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        JPanel metaPanel = new JPanel();
        metaPanel.setLayout(new MigLayout("fill, insets 0 0 0 0", "[grow]", "[grow][30!]"));
        
        mainPanel = new JPanel();
    
        mainLayout = new CardLayout();
        mainPanel.setLayout(mainLayout);
    
        recordingListPanel = new RecordingListPanel(settings);
        clipListPanel = new ClipListPanel(settings);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Recordings", recordingListPanel);
        tabbedPane.add("Clips", clipListPanel);
        tabbedPane.add("Settings", new SettingsPanel());
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        
        clipQueue = new ClipQueue(new ClipTrimmer(settings));
        uploadQueue = new UploadQueue(new YoutubeUploader(settings));
        
        clippingPanel = new ClippingPanel(clipQueue);
        uploadPanel = new UploadPanel(uploadQueue);
        
        mainPanel.add(tabbedPane, "mainView");
        mainPanel.add(clippingPanel, "clippingView");
        mainPanel.add(uploadPanel, "uploadView");
        mainLayout.show(mainPanel, "mainView");
        
        statusPanel = new StatusPanel();
        StatusQueue.setConsumer(statusPanel);
        
        metaPanel.add(mainPanel, "cell 0 0, grow");
        metaPanel.add(statusPanel, "cell 0 1, growx");
        
        mainFrame.getContentPane().add(metaPanel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        clipQueue.startProcessing();
        uploadQueue.startProcessing();
    
        tabbedPane.addChangeListener(e -> {
            int tabIndex = tabbedPane.getSelectedIndex();
            if(tabIndex == tabbedPane.indexOfTab("Recordings"))
            {
                recordingListPanel.update();
            }
            else if(tabIndex == tabbedPane.indexOfTab("Clips"))
            {
                clipListPanel.update();
            }
        });
        
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                clipQueue.stopProcessing();
                uploadQueue.stopProcessing();
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
        clippingPanel.setRecording(videoFile);
        mainLayout.show(mainPanel, "clippingView");
    }
    
    public void showUploadView(File videoFile)
    {
        System.out.println("showing uploadView");
        uploadPanel.setRecording(videoFile);
        mainLayout.show(mainPanel, "uploadView");
    }
    
    public void showMainView()
    {
        mainLayout.show(mainPanel, "mainView");
    }
}
