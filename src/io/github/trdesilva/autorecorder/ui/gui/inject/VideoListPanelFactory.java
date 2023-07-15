/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.inject;

import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.VideoInfoPanel;
import io.github.trdesilva.autorecorder.video.VideoListHandler;

public interface VideoListPanelFactory
{
    VideoListPanel create(VideoListHandler videoListHandler, VideoInfoPanel selectionConsumer);
}
