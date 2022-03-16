/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.clip;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ClipJob
{
    private String source;
    private String dest;
    private List<String> startArgs;
    private List<String> endArgs;
    private boolean segmented;
    
    public ClipJob(String source, String dest, String startArg, String endArg)
    {
        this.source = source;
        this.dest = dest;
        this.startArgs = Collections.singletonList(startArg);
        this.endArgs = Collections.singletonList(endArg);
        
        this.segmented = false;
    }
    
    public ClipJob(String source, String dest, List<String> startArgs, List<String> endArgs)
    {
        this.source = source;
        this.dest = dest;
        this.startArgs = new LinkedList<>(startArgs);
        this.endArgs = new LinkedList<>(endArgs);
        
        this.segmented = true;
    }
    
    public String getSource()
    {
        return source;
    }
    
    public String getDest()
    {
        return dest;
    }
    
    public List<String> getStartArgs()
    {
        return startArgs;
    }
    
    public List<String> getEndArgs()
    {
        return endArgs;
    }
    
    public boolean isSegmented()
    {
        return segmented;
    }
    
    @Override
    public String toString()
    {
        return "ClipJob{" +
                "source='" + source + '\'' +
                ", dest='" + dest + '\'' +
                ", startArg='" + startArgs + '\'' +
                ", endArg='" + endArgs + '\'' +
                ", segmented='" + segmented + '\'' +
                '}';
    }
}
