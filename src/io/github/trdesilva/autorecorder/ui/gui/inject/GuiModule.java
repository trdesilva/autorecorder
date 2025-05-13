/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.inject;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.upload.Uploader;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;
import io.github.trdesilva.autorecorder.video.inject.VideoListHandlerFactory;
import org.apache.commons.io.IOUtils;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ResourceBundle;

public class GuiModule extends AbstractModule
{
    private final boolean isDebugMode;
    private final FlatLaf lookAndFeel;
    
    public GuiModule(boolean isDebugMode)
    {
        this.isDebugMode = isDebugMode;
        FlatLaf.registerCustomDefaultsSource("io.github.trdesilva.autorecorder.ui.gui");
    
        try
        {
            Font defaultFont = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemClassLoader().getResourceAsStream("RobotoFlex.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(defaultFont);
        }
        catch(Exception e)
        {
            System.err.println("Failed to register default font: " + e.getLocalizedMessage());
        }
        lookAndFeel = new FlatDarkLaf();
    }
    
    @Override
    protected void configure()
    {
        bind(LookAndFeel.class).toInstance(lookAndFeel);
        try
        {
            UIManager.setLookAndFeel(lookAndFeel);
        }
        catch(UnsupportedLookAndFeelException e)
        {
            System.err.println("Failed to set FlatLaf as look and feel: " + e.getLocalizedMessage());
        }
        
        requireBinding(VideoListHandlerFactory.class);
        
        install(new FactoryModuleBuilder().build(VideoListPanelFactory.class));
        bind(Uploader.class).to(YoutubeUploader.class);
        bind(Navigator.class).to(MainWindow.class);
        bindConstant().annotatedWith(Names.named("isDebugMode")).to(isDebugMode);
    }
}
