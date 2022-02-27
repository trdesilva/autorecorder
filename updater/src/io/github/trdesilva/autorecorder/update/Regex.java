/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.update;

import java.util.regex.Pattern;

public class Regex
{
    public static final Pattern JAR_PATTERN = Pattern.compile("autorecorder_v(\\d+)_(\\d+)(?:_(\\d+))?\\.jar");
    public static final Pattern TAG_PATTERN = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)?");
}
