/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

public class VideoFilenameValidator
{
    public boolean hasValidName(String name)
    {
        return name != null && name.matches("\\w[\\w \\-]*\\.(?:mpg|mpeg|mp4|mkv|mov|avi|wmv)");
    }
}
