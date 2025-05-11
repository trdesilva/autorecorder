/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicLongMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventConsumer;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.WrappingLabel;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import io.github.trdesilva.autorecorder.video.VideoMetadataHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoListPanel extends JScrollPane implements EventConsumer
{
    public enum SortOrder
    {
        NAME,
        DATE
    }
    
    public enum DisplayType
    {
        NAMES,
        TILES
    }
    
    private static final Set<EventType> EVENT_TYPES = Sets.immutableEnumSet(EventType.RECORDING_END);
    
    private final EventQueue events;
    private final VideoListCellRenderer renderer;
    private VideoListHandler videoListHandler;
    
    private final JList<File> videos;
    private SortOrder sortOrder;
    
    @AssistedInject
    public VideoListPanel(EventQueue events, VideoListCellRenderer renderer, @Assisted VideoListHandler videoListHandler, @Assisted VideoInfoPanel selectionConsumer)
    {
        this.events = events;
        this.renderer = renderer;
        
        this.videoListHandler = videoListHandler;
        videos = new JList<>(videoListHandler.getVideoList().toArray(new File[0]));
        videos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.renderer.setParent(this);
        videos.setCellRenderer(renderer);
        videos.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        videos.setVisibleRowCount(-1);
        
        sortOrder = SortOrder.DATE;
        
        videos.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if(e.getValueIsAdjusting())
                {
                    return;
                }
                selectionConsumer.setVideo(videos.getSelectedValue());
            }
        });
        getViewport().add(videos);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e)
            {
                if(videos.getCellRenderer().equals(renderer))
                {
                    resetListCellSizes();
                }
                else
                {
                    videos.setFixedCellWidth(-1);
                    videos.setFixedCellHeight(-1);
                    videos.setVisibleRowCount(256);
                }
            }
        });
        
        events.addConsumer(this);
    }
    
    public void updateList(SortOrder sortOrder)
    {
        List<File> videoList = videoListHandler.getVideoList();
        if(sortOrder != null)
        {
            this.sortOrder = sortOrder;
            switch(sortOrder)
            {
                case NAME:
                    videoList = videoList.stream()
                                         .sorted(Comparator.comparing((file) -> file.getName().toLowerCase()))
                                         .collect(Collectors.toList());
                    break;
                case DATE:
                    videoList = videoList.stream()
                                         .sorted(Comparator.comparing(File::lastModified).reversed())
                                         .collect(Collectors.toList());
                    break;
            }
        }
        File[] videoArray = videoList.toArray(new File[0]);
        videos.setListData(videoArray);
    }
    
    public void changeDisplayType(DisplayType type)
    {
        if(type == DisplayType.TILES)
        {
            videos.setCellRenderer(renderer);
            videos.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            resetListCellSizes();
        }
        else
        {
            videos.setCellRenderer(new DefaultListCellRenderer());
            videos.setLayoutOrientation(JList.VERTICAL);
            videos.setFixedCellWidth(-1);
            videos.setFixedCellHeight(-1);
            videos.setVisibleRowCount(256);
        }
    }
    
    private void resetListCellSizes()
    {
        int oldCellWidth = videos.getFixedCellWidth();
        int newCellWidth = (int) (Math.min(340, getWidth() / 4. - 5));
        if(oldCellWidth != newCellWidth)
        {
            videos.setFixedCellWidth(newCellWidth);
            videos.setFixedCellHeight(30 + videos.getFixedCellWidth() * 9 / 16);
            videos.setVisibleRowCount(videos.getModel().getSize() / (getWidth() / newCellWidth) + 1);
            renderer.clearCaches();
        }
    }
    
    @Override
    public void post(Event event)
    {
        if(event.getType().equals(EventType.RECORDING_END))
        {
            SwingUtilities.invokeLater(() -> updateList(sortOrder));
        }
    }
    
    @Override
    public Set<EventType> getSubscriptions()
    {
        return EVENT_TYPES;
    }
    
    private static class VideoListCellRenderer implements ListCellRenderer<File>
    {
        private final VideoMetadataHandler metadataHandler;
        private final AtomicLongMap<File> renderStatusMap;
        private final AtomicLongMap<File> loadAttempts;
        private final Map<File, JComponent> resultCache;
        private JComponent parent;
        
        @Inject
        public VideoListCellRenderer(VideoMetadataHandler metadataHandler)
        {
            this.metadataHandler = metadataHandler;
            renderStatusMap = AtomicLongMap.create();
            loadAttempts = AtomicLongMap.create();
            resultCache = new HashMap<>();
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends File> list, File value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            JComponent result;
            synchronized(value)
            {
                if(renderStatusMap.get(value) == 2)
                {
                    result = resultCache.get(value);
                }
                else
                {
                    result = new JPanel();
                    JLabel thumbnailLabel = new JLabel();
                    WrappingLabel titleLabel = new WrappingLabel(value.getName());
                    int extIndex = value.getName().indexOf('.');
                    if(extIndex != -1)
                    {
                        titleLabel.setText(value.getName().substring(0, extIndex));
                    }
                    GridBagLayout layout = new GridBagLayout();
                    
                    result.setLayout(layout);
    
                    int imageWidth = list.getFixedCellWidth() - 10;
                    Image image = null;
                    if(renderStatusMap.get(value) == 0)
                    {
                        image = metadataHandler.getThumbnail(value, false);
                    }
    
                    if(image != null)
                    {
                        Image scaledImage = image.getScaledInstance(imageWidth, (int) (imageWidth * 9. / 16),
                                                                    Image.SCALE_SMOOTH);
                        thumbnailLabel.setIcon(new ImageIcon(scaledImage));
                        thumbnailLabel.setText("");
                        renderStatusMap.put(value, 2);
                    }
                    else
                    {
                        thumbnailLabel.setText("Loading...");
                        if(renderStatusMap.get(value) == 0 && loadAttempts.getAndIncrement(value) < 5)
                        {
                            renderStatusMap.put(value, 1);
                            SwingUtilities.invokeLater(() -> {
                                metadataHandler.getThumbnail(value, true);
                                if(renderStatusMap.get(value) == 1)
                                {
                                    renderStatusMap.put(value, 0);
                                }
                                list.repaint();
                            });
                        }
                    }
    
                    result.add(thumbnailLabel);
                    result.add(titleLabel);
                    
                    GridBagConstraints thumbnailConstraints = new GridBagConstraints();
                    thumbnailConstraints.fill = GridBagConstraints.NONE;
                    thumbnailConstraints.anchor = GridBagConstraints.CENTER;
                    thumbnailConstraints.gridy = 1;
                    layout.setConstraints(thumbnailLabel, thumbnailConstraints);
    
                    GridBagConstraints titleConstraints = new GridBagConstraints();
                    titleConstraints.fill = GridBagConstraints.BOTH;
                    titleConstraints.anchor = GridBagConstraints.CENTER;
                    titleConstraints.gridy = 2;
                    layout.setConstraints(titleLabel, titleConstraints);
                    
                    GridBagConstraints panelConstraints = new GridBagConstraints();
                    panelConstraints.fill = GridBagConstraints.BOTH;
                    panelConstraints.insets = new Insets(5, 5, 5, 5);
                    layout.setConstraints(result, panelConstraints);
    
                    resultCache.put(value, result);
                }
            }
    
            if(isSelected)
            {
                result.setBackground(UIManager.getColor("List.selectionBackground"));
            }
            else
            {
                result.setBackground(UIManager.getColor("List.background"));
            }
            
            return result;
        }
        
        public void setParent(JComponent parent)
        {
            this.parent = parent;
        }
        
        public void clearCaches()
        {
            renderStatusMap.clear();
            loadAttempts.clear();
            resultCache.clear();
        }
    }
}
