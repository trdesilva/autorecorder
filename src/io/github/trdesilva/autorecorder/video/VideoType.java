/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import io.github.trdesilva.autorecorder.Settings;

public enum VideoType
{
    CLIP,
    RECORDING;
    
    public String getPathSetting(Settings settings)
    {
        switch(this)
        {
            case CLIP:
                return settings.getClipPath();
            case RECORDING:
                return settings.getRecordingPath();
        }
        return null;
    }
}
