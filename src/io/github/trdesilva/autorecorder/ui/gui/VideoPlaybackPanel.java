/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import net.miginfocom.swing.MigLayout;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.trdesilva.autorecorder.TimestampUtil.formatTime;
import static io.github.trdesilva.autorecorder.TimestampUtil.parseTime;

public class VideoPlaybackPanel extends DefaultPanel implements AutoCloseable
{
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final SeekBar seekBar;
    
    private final JButton slowerButton;
    private final JTextField speedField;
    private final JButton fasterButton;
    
    private final JButton playPauseButton;
    private final JButton longRewindButton;
    private final JButton mediumRewindButton;
    private final JButton shortRewindButton;
    private final JButton shortFastForwardButton;
    private final JButton mediumFastForwardButton;
    private final JButton longFastForwardButton;
    
    private final JSlider volumeBar;
    
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private float playbackRate;
    private int frameRate;
    private Thread subsectionControlThread;
    
    @Inject
    public VideoPlaybackPanel(WindowCloseHandler windowCloseHandler)
    {
        setLayout(new MigLayout("", "[grow]", "[grow][60:6.25%:100]"));
        setPreferredSize(new Dimension(MainWindow.PREFERRED_WIDTH, 10 * MainWindow.PREFERRED_WIDTH / 16));
        
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setPreferredSize(
                new Dimension(MainWindow.PREFERRED_WIDTH, 9 * MainWindow.PREFERRED_WIDTH / 16));
        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter()
        {
            @Override
            public void stopped(MediaPlayer mediaPlayer)
            {
                SwingUtilities.invokeLater(() -> {
                    mediaPlayer.submit(() -> {
                        seekBar.changeTime(0);
                        setIsPlaying(false);
                        seekBar.refresh();
                    });
                });
            }
        });
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new MigLayout("fill", "[grow, center]", "[][]"));
        seekBar = new SeekBar();
        
        slowerButton = new JButton("-");
        slowerButton.setToolTipText("Halve current playback speed");
        
        speedField = new JTextField("1.000x");
        speedField.setToolTipText("Playback speed (click to reset)");
        speedField.setEditable(false);
        speedField.setHorizontalAlignment(SwingConstants.CENTER);
        
        fasterButton = new JButton("+");
        fasterButton.setToolTipText("Double current playback speed");
        
        playPauseButton = new JButton("Pause");
        
        longRewindButton = new JButton("<<<");
        longRewindButton.setToolTipText("Rewind 10 seconds");
        mediumRewindButton = new JButton("<<");
        mediumRewindButton.setToolTipText("Rewind 1 second");
        shortRewindButton = new JButton("<");
        shortRewindButton.setToolTipText("Rewind ~1 frame");
        
        longFastForwardButton = new JButton(">>>");
        longFastForwardButton.setToolTipText("Fast-forward 10 seconds");
        mediumFastForwardButton = new JButton(">>");
        mediumFastForwardButton.setToolTipText("Fast-forward 1 second");
        shortFastForwardButton = new JButton(">");
        shortFastForwardButton.setToolTipText("Fast-forward ~1 frame");
        
        volumeBar = new JSlider(0, 150, 100);
        volumeBar.setToolTipText("100%");
        
        JLabel volumeLabel = new JLabel("Vol");
        
        windowCloseHandler.addCloseable(this);
        
        controlPanel.add(seekBar, "cell 0 0, growx, id seekBar");
        
        controlPanel.add(slowerButton, "cell 0 1, pos seekbar.x seekbar.y2, id slower");
        controlPanel.add(speedField, "cell 0 1, pos slower.x2 slower.y, w 60:60:80, id speed");
        controlPanel.add(fasterButton, "cell 0 1, pos speed.x2 slower.y, id faster");
        
        controlPanel.add(playPauseButton, "cell 0 1, align center, id play");
        controlPanel.add(shortRewindButton, "cell 0 1, pos null play.y play.x null, id shortRewind");
        controlPanel.add(mediumRewindButton, "cell 0 1, pos null play.y shortRewind.x null, id mediumRewind");
        controlPanel.add(longRewindButton, "cell 0 1, pos null play.y mediumRewind.x null, id longRewind");
        controlPanel.add(shortFastForwardButton, "cell 0 1, pos play.x2 play.y, id shortFastForward");
        controlPanel.add(mediumFastForwardButton, "cell 0 1, pos shortFastForward.x2 play.y, id mediumFastForward");
        controlPanel.add(longFastForwardButton, "cell 0 1, pos mediumFastForward.x2 play.y, id longFastForward");
        
        controlPanel.add(volumeBar, "cell 0 1, pos null seekbar.y2 visual.x2 null, w 100!, id volumeBar");
        controlPanel.add(volumeLabel, "cell 0 1, pos null volumeBar.y volumeBar.x volumeBar.y2");
        
        add(mediaPlayerComponent, "span, grow, wmin 400, hmin 225");
        add(controlPanel, "span, grow");
        
        slowerButton.addActionListener(e -> {
            setPlaybackRate(playbackRate/2);
        });
        
        speedField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setPlaybackRate(1.0f);
            }
        });
        
        fasterButton.addActionListener(e -> {
            setPlaybackRate(playbackRate*2);
        });
        
        playPauseButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setIsPlaying(!isPlaying.get());
                seekBar.refresh();
            }
        });
        
        shortRewindButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(-1000 / Math.max(10, frameRate));
            seekBar.refresh();
        });
        
        mediumRewindButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(-1000);
            seekBar.refresh();
        });
        
        longRewindButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000);
            seekBar.refresh();
        });
        
        shortFastForwardButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(1000 / Math.max(10, frameRate));
            seekBar.refresh();
        });
        
        mediumFastForwardButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(1000);
            seekBar.refresh();
        });
        
        longFastForwardButton.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(10000);
            seekBar.refresh();
        });
        
        volumeBar.addChangeListener(e -> {
            mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeBar.getValue());
            volumeBar.setToolTipText(volumeBar.getValue() + "%");
        });
    }
    
    public synchronized void setIsPlaying(boolean isPlaying)
    {
        if(!isPlaying)
        {
            playPauseButton.setText("Play");
            mediaPlayerComponent.mediaPlayer().controls().pause();
        }
        else
        {
            playPauseButton.setText("Pause");
            mediaPlayerComponent.mediaPlayer().controls().play();
        }
        
        this.isPlaying.set(isPlaying);
    }
    
    public void play(File videoFile)
    {
        mediaPlayerComponent.mediaPlayer().media().prepare(videoFile.getAbsolutePath());
        setIsPlaying(true);
        setPlaybackRate(1.0f);
        
        // mediaPlayer needs a moment to load metadata, so spin on it
        new Thread(() -> {
            long duration = mediaPlayerComponent.mediaPlayer().media().info().duration();
            while(duration == -1)
            {
                duration = mediaPlayerComponent.mediaPlayer().media().info().duration();
            }
            seekBar.setDuration(duration);
            VideoTrackInfo track = mediaPlayerComponent.mediaPlayer()
                                                       .media()
                                                       .info()
                                                       .videoTracks()
                                                       .get(mediaPlayerComponent.mediaPlayer().video().track());
            frameRate = track.frameRate() / track.frameRateBase();
        }).start();
    }
    
    public void stop()
    {
        mediaPlayerComponent.mediaPlayer().controls().stop();
    }
    
    public long getPlaybackTime()
    {
        return mediaPlayerComponent.mediaPlayer().status().time();
    }
    
    public void playSubsection(long start, long end)
    {
        if(subsectionControlThread != null && subsectionControlThread.isAlive())
        {
            subsectionControlThread.interrupt();
        }
        
        subsectionControlThread = new Thread(() -> {
            try
            {
                seekBar.changeTime(start);
                setIsPlaying(true);
                Thread.sleep(end - mediaPlayerComponent.mediaPlayer().status().time() - 1000);
                while(mediaPlayerComponent.mediaPlayer().status().time() < end)
                {
                    Thread.sleep(50);
                }
                setIsPlaying(false);
            }
            catch(InterruptedException e)
            {
                return;
            }
        });
        
        subsectionControlThread.start();
    }
    
    public void playSubsections(List<Long> starts, List<Long> ends)
    {
        if(starts.size() != ends.size())
        {
            throw new IllegalArgumentException("how did we get an uneven number of starts and ends in here");
        }
    
        if(subsectionControlThread != null && subsectionControlThread.isAlive())
        {
            subsectionControlThread.interrupt();
        }
        
        subsectionControlThread = new Thread(() -> {
            for(int i = 0; i < starts.size(); i++)
            {
                long start = starts.get(i);
                long end = ends.get(i);
                try
                {
                    seekBar.changeTime(start);
                    setIsPlaying(true);
                    Thread.sleep(end - mediaPlayerComponent.mediaPlayer().status().time() - 1000);
                    while(mediaPlayerComponent.mediaPlayer().status().time() < end)
                    {
                        Thread.sleep(50);
                    }
                    setIsPlaying(false);
                }
                catch(InterruptedException e)
                {
                    return;
                }
            }
        });
    
        subsectionControlThread.start();
    }
    
    public void setPlaybackRate(float playbackRate)
    {
        // set bounds to powers of two that fit in %2.3f
        this.playbackRate = Math.max(Math.min(playbackRate, 64.0f), 0.001953125f);
        mediaPlayerComponent.mediaPlayer().controls().setRate(this.playbackRate);
        speedField.setText(String.format("%2.3fx", this.playbackRate));
    }
    
    public void seekTo(long time, boolean forceStart)
    {
        // TODO fix weird behavior with bookmarks after playing has finished
        if(subsectionControlThread != null && subsectionControlThread.isAlive())
        {
            subsectionControlThread.interrupt();
        }
        
        seekBar.changeTime(time);
    
        if(forceStart && !isPlaying.get())
        {
            setIsPlaying(true);
        }
    }
    
    @Override
    public void close() throws IOException
    {
        seekBar.refreshThread.interrupt();
        mediaPlayerComponent.release();
    }
    
    /*
     * Adapted from uk.co.caprica.vlcjplayer.view.main.PositionPane.
     * See https://github.com/caprica/vlcj-player/blob/master/src/main/java/uk/co/caprica/vlcjplayer/view/main/PositionPane.java
     */
    private class SeekBar extends DefaultPanel
    {
        
        private final JTextField timeField;
        private final JSlider positionSlider;
        private final JLabel durationLabel;
        
        private long time;
        private long duration;
        private final AtomicBoolean sliderChanging = new AtomicBoolean();
        private final AtomicBoolean positionChanging = new AtomicBoolean();
        
        Thread refreshThread;
        
        public SeekBar()
        {
            timeField = new JTextField("9:99:99");
            timeField.setColumns(5);
            timeField.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    super.focusLost(e);
                    changeTime(parseTime(timeField.getText()));
                    refresh();
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
                        sliderChanging.set(source.getValueIsAdjusting());
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
        
        synchronized void changeTime(long time)
        {
            if(time != -1)
            {
                this.time = Math.max(Math.min(time, duration), 0);
                mediaPlayerComponent.mediaPlayer().controls().setTime(this.time);
            }
        }
        
        void setDuration(long duration)
        {
            this.duration = duration;
            durationLabel.setText(formatTime(duration));
        }
    }
}
