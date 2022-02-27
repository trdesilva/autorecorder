/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.update;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;

import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.trdesilva.autorecorder.update.Regex.JAR_PATTERN;
import static io.github.trdesilva.autorecorder.update.Regex.TAG_PATTERN;

public class VersionNumber implements Comparable<VersionNumber>
{
    
    public static VersionNumber fromTag(String tag)
    {
        return parseVersionNumber(tag, TAG_PATTERN);
    }
    
    public static VersionNumber fromJarName(String jarName)
    {
        return parseVersionNumber(jarName, JAR_PATTERN);
    }
    
    private static VersionNumber parseVersionNumber(String string, Pattern pattern)
    {
        if(string != null)
        {
            Matcher matcher = pattern.matcher(string);
            if(matcher.matches())
            {
                MatchResult result = matcher.toMatchResult();
                if(result.groupCount() >= 2)
                {
                    int major = Integer.parseInt(result.group(1));
                    int minor = Integer.parseInt(result.group(2));
                    int patch = 0;
                    if(result.groupCount() == 3)
                    {
                        patch = Integer.parseInt(result.group(3));
                    }
                    return new VersionNumber(major, minor, patch);
                }
            }
        }
        return new VersionNumber(0, 0, 0);
    }
    
    private int major;
    private int minor;
    private int patch;
    
    public VersionNumber(int major, int minor, int patch)
    {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }
    
    public int getMajor()
    {
        return major;
    }
    
    public int getMinor()
    {
        return minor;
    }
    
    public int getPatch()
    {
        return patch;
    }
    
    @Override
    public int compareTo(VersionNumber o)
    {
        // none of these numbers should go over 100, so major > minor > patch
        return 100 * 100 * (major - o.major) + 100 * (minor - o.minor) + (patch - o.patch);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        VersionNumber that = (VersionNumber) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(major, minor, patch);
    }
    
    @Override
    public String toString()
    {
        return String.format("v%d.%d.%d", major, minor, patch);
    }
}
