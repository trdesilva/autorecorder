/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.event;

public enum EventType
{
    SUCCESS,
    FAILURE,
    WARNING,
    INFO,
    RECORDING_START,
    RECORDING_END,
    CLIP_START,
    CLIP_END,
    UPLOAD_START,
    UPLOAD_END,
    MANUAL_RECORDING_START,
    MANUAL_RECORDING_END,
    SETTINGS_CHANGE,
    BOOKMARK,
    DEBUG,
    THUMBNAIL_GENERATED
}
