/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.inject;

import io.github.trdesilva.autorecorder.ui.gui.VideoListPanel;
import io.github.trdesilva.autorecorder.ui.gui.VideoListSelectionConsumer;

import java.io.File;

public interface VideoListPanelFactory
{
    VideoListPanel create(File videoDir, VideoListSelectionConsumer selectionConsumer);
}
