package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.clip.ClipQueue;
import io.github.trdesilva.autorecorder.clip.ClipTrimmer;
import io.github.trdesilva.autorecorder.ui.status.StatusQueue;
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
    
    private final ClippingPanel clippingPanel;
    private final StatusPanel statusPanel;
    
    private ClipQueue clipQueue;
    
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
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Recordings", new RecordingListPanel(settings));
        tabbedPane.add("Clips", new ClipListPanel(settings));
        tabbedPane.add("Settings", new SettingsPanel());
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        
        clipQueue = new ClipQueue(new ClipTrimmer(settings));
        
        clippingPanel = new ClippingPanel(clipQueue);
        
        mainPanel.add(tabbedPane, "mainView");
        mainPanel.add(clippingPanel, "clippingView");
        mainLayout.show(mainPanel, "mainView");
        
        statusPanel = new StatusPanel();
        StatusQueue.getInstance().setConsumer(statusPanel);
        
        metaPanel.add(mainPanel, "cell 0 0, grow");
        metaPanel.add(statusPanel, "cell 0 1, growx");
        
        mainFrame.getContentPane().add(metaPanel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        clipQueue.startProcessing();
        
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                clipQueue.stopProcessing();
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
