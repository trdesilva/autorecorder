/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video.inject;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.trdesilva.autorecorder.video.inject.VideoListHandlerFactory;

public class VideoModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        install(new FactoryModuleBuilder().build(VideoListHandlerFactory.class));
    }
}
