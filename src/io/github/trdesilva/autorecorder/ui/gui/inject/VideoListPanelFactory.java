/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.inject;

import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;
import io.github.trdesilva.autorecorder.video.VideoListHandler;
import io.github.trdesilva.autorecorder.video.VideoType;

import java.io.File;

public interface VideoListPanelFactory
{
    VideoListPanel create(VideoType videoType, VideoListSelectionConsumer selectionConsumer);
}
