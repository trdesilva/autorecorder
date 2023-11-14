/*
 * Copyright (c) 2023 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.trdesilva.autorecorder.event.Event;
import io.github.trdesilva.autorecorder.event.EventQueue;
import io.github.trdesilva.autorecorder.event.EventType;
import io.github.trdesilva.autorecorder.ui.gui.ReportableException;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeServiceFactory;
import io.github.trdesilva.autorecorder.upload.youtube.YoutubeUploader;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class YoutubeMetadataRefresher
{
    private final YoutubeServiceFactory youtubeServiceFactory;
    private final VideoMetadataHandler metadataHandler;
    private final VideoListHandler clipHandler;
    private final EventQueue events;
    
    @Inject
    public YoutubeMetadataRefresher(YoutubeServiceFactory youtubeServiceFactory, VideoMetadataHandler metadataHandler,
                                    @Named("CLIP") VideoListHandler clipHandler, EventQueue events)
    {
        this.youtubeServiceFactory = youtubeServiceFactory;
        this.metadataHandler = metadataHandler;
        this.clipHandler = clipHandler;
        this.events = events;
    }
    
    public void refreshMetadata() throws ReportableException, GeneralSecurityException, IOException
    {
        events.postEvent(new Event(EventType.DEBUG, "Starting Youtube metadata refresh"));
        YouTube youtubeService = youtubeServiceFactory.getService();
        List<File> clipList = clipHandler.getVideoList();
        
        // API ToS requires that API data be refreshed periodically unless it comes from the Statistics or Reporting APIs
        List<String> clipIds = clipList.stream()
                                       .map((file) -> metadataHandler.getMetadata(file).getUploadLink())
                                       .filter(Predicate.not(String::isBlank))
                                       .map(link -> link.substring(YoutubeUploader.VIDEO_URL_PREFIX.length()))
                                       .collect(Collectors.toList());
        List<Video> youtubeApiResponse = youtubeService.videos()
                                             .list(Arrays.asList("id"))
                                             .setId(clipIds)
                                             .execute()
                                             .getItems();
        Set<String> validIdSet = youtubeApiResponse.stream().map(Video::getId).collect(Collectors.toSet());
        
        for(File videoFile: clipList)
        {
            VideoMetadata metadata = metadataHandler.getMetadata(videoFile);
            if(!metadata.getUploadLink().isBlank()
            && metadata.getUploadLink().startsWith(YoutubeUploader.VIDEO_URL_PREFIX))
            {
                String id = metadata.getUploadLink().substring(YoutubeUploader.VIDEO_URL_PREFIX.length());
                if(!validIdSet.contains(id))
                {
                    events.postEvent(new Event(EventType.DEBUG, String.format("Video %s not found in user's video list; removing link from metadata", id)));
                    metadata.setUploadLink("");
                    metadataHandler.saveMetadata(videoFile, metadata);
                }
            }
        }
        events.postEvent(new Event(EventType.DEBUG, "Youtube metadata refresh done"));
    }
}
