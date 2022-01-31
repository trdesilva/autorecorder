package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoPlaybackPanel extends JPanel implements AutoCloseable
{
    private final SeekBar seekBar;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    
    public VideoPlaybackPanel()
    {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(MainWindow.PREFERRED_WIDTH, 10 * MainWindow.PREFERRED_WIDTH / 16));
        
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setPreferredSize(new Dimension(MainWindow.PREFERRED_WIDTH, 9 * MainWindow.PREFERRED_WIDTH / 16));
        
        JPanel controlPanel = new JPanel();
        seekBar = new SeekBar();
        JButton playPauseButton = new JButton("Pause");
        playPauseButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(isPlaying.get())
                {
                    playPauseButton.setText("Play");
                    mediaPlayerComponent.mediaPlayer().controls().pause();
                }
                else
                {
                    playPauseButton.setText("Pause");
                    mediaPlayerComponent.mediaPlayer().controls().play();
                }
                
                isPlaying.set(!isPlaying.get());
            }
        });
        
        MainWindow.closeables.add(this);
        
        controlPanel.add(seekBar);
        controlPanel.add(playPauseButton);
        
        add(mediaPlayerComponent, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    public void play(File videoFile)
    {
        System.out.println("playing " + videoFile);
        mediaPlayerComponent.mediaPlayer().media().play(videoFile.getAbsolutePath());
        
        // mediaPlayer needs a moment to load metadata, so spin on it
        long duration = mediaPlayerComponent.mediaPlayer().media().info().duration();
        while(duration == -1)
        {
            duration = mediaPlayerComponent.mediaPlayer().media().info().duration();
        }
        seekBar.setDuration(duration);
        isPlaying.set(true);
    }
    
    @Override
    public void close() throws IOException
    {
        seekBar.refreshThread.interrupt();
        mediaPlayerComponent.release();
    }
    
    private class SeekBar extends JPanel
    {
        
        private final JTextField timeField;
        private final JSlider positionSlider;
        private final JLabel durationLabel;
        
        private long time;
        private final AtomicBoolean sliderChanging = new AtomicBoolean();
        private final AtomicBoolean positionChanging = new AtomicBoolean();
        
        Thread refreshThread;
        
        public SeekBar()
        {
            timeField = new JTextField("9:99:99");
            timeField.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    super.focusLost(e);
                    changeTime();
                }
            });
            
            UIManager.put("Slider.paintValue", false); // FIXME how to do this for a single component?
            positionSlider = new JSlider();
            positionSlider.setMinimum(0);
            positionSlider.setMaximum(1000);
            positionSlider.setValue(0);
            
            positionSlider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    if(!positionChanging.get())
                    {
                        JSlider source = (JSlider) e.getSource();
                        if(source.getValueIsAdjusting())
                        {
                            sliderChanging.set(true);
                        }
                        else
                        {
                            sliderChanging.set(false);
                        }
                        mediaPlayerComponent.mediaPlayer().controls().setPosition(source.getValue() / 1000.0f);
                    }
                }
            });
            
            durationLabel = new JLabel("9:99:99");
            
            setLayout(new MigLayout("fill, insets 0 0 0 0", "[][grow][]", "[]"));
            
            add(timeField, "shrink");
            add(positionSlider, "grow");
            add(durationLabel, "shrink");
            
            timeField.setText("-:--:--");
            durationLabel.setText("-:--:--");
            
            refreshThread = new Thread(() -> {
                while(true)
                {
                    try
                    {
                        if(isPlaying.get())
                        {
                            refresh();
                            if(timeField.isEditable())
                            {
                                timeField.setEditable(false);
                            }
                        }
                        else
                        {
                            if(!timeField.isEditable())
                            {
                                timeField.setEditable(true);
                            }
                        }
                        Thread.sleep(500);
                    }
                    catch(InterruptedException e)
                    {
                        return;
                    }
                }
            });
            refreshThread.start();
        }
        
        private void refresh()
        {
            time = mediaPlayerComponent.mediaPlayer().status().time();
            timeField.setText(formatTime(time));
            
            if(!sliderChanging.get())
            {
                int value = (int) (mediaPlayerComponent.mediaPlayer().status().position() * 1000.0f);
                positionChanging.set(true);
                positionSlider.setValue(value);
                positionChanging.set(false);
            }
        }
        
        void changeTime()
        {
            String timestamp = timeField.getText();
            long time = parseTime(timestamp);
            if(time != -1)
            {
                mediaPlayerComponent.mediaPlayer().controls().setTime(time);
            }
        }
        
        void setDuration(long duration)
        {
            durationLabel.setText(formatTime(duration));
        }
        
        private String formatTime(long millis)
        {
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis % TimeUnit.HOURS.toMillis(1));
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis % TimeUnit.MINUTES.toMillis(1));
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        
        private long parseTime(String timestamp)
        {
            String[] numbers = timestamp.split(":");
            
            if(numbers.length == 3)
            {
                long millis = TimeUnit.SECONDS.toMillis(Long.parseLong(numbers[2]));
                millis += TimeUnit.MINUTES.toMillis(Long.parseLong(numbers[1]));
                millis += TimeUnit.HOURS.toMillis(Long.parseLong(numbers[0]));
                
                return millis;
            }
            return -1;
        }
    }
}
