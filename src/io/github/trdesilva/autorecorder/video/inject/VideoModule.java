/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.github.trdesilva.autorecorder.ui.gui.record.RecordingListPanel;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import io.github.trdesilva.autorecorder.video.VideoType;

public class VideoModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        install(new FactoryModuleBuilder().build(VideoListHandlerFactory.class));
        bind(VideoListHandler.class).annotatedWith(Names.named(VideoType.CLIP.name())).toProvider(ClipListHandlerProvider.class);
        bind(VideoListHandler.class).annotatedWith(Names.named(VideoType.RECORDING.name())).toProvider(RecordingListHandlerProvider.class);
    }
    
    @Singleton
    private static class ClipListHandlerProvider implements Provider<VideoListHandler>
    {
        private final VideoListHandler clipListHandler;
        
        @Inject
        public ClipListHandlerProvider(VideoListHandlerFactory factory)
        {
            this.clipListHandler = factory.create(VideoType.CLIP);
        }
        
        @Override
        public VideoListHandler get()
        {
            return clipListHandler;
        }
    }
    
    @Singleton
    private static class RecordingListHandlerProvider implements Provider<VideoListHandler>
    {
        private final VideoListHandler recordingListHandler;
        
        @Inject
        public RecordingListHandlerProvider(VideoListHandlerFactory factory)
        {
            this.recordingListHandler = factory.create(VideoType.RECORDING);
        }
        
        @Override
        public VideoListHandler get()
        {
            return recordingListHandler;
        }
    }
}
