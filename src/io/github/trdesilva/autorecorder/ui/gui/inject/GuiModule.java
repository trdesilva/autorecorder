/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.github.trdesilva.autorecorder.ui.gui.MainWindow;
import io.github.trdesilva.autorecorder.ui.gui.Navigator;
import io.github.trdesilva.autorecorder.upload.Uploader;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;

public class GuiModule extends AbstractModule
{
    private final boolean isDebugMode;
    
    public GuiModule(boolean isDebugMode)
    {
        this.isDebugMode = isDebugMode;
    }
    
    @Override
    protected void configure()
    {
        install(new FactoryModuleBuilder().build(VideoListPanelFactory.class));
        bind(Uploader.class).to(YoutubeUploader.class);
        bind(Navigator.class).to(MainWindow.class);
        bindConstant().annotatedWith(Names.named("isDebugMode")).to(isDebugMode);
    }
}
