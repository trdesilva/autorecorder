package io.github.trdesilva.autorecorder.clip;

public class ClipJob
{
    private String source;
    private String dest;
    private String startArg;
    private String endArg;
    
    public ClipJob(String source, String dest, String startArg, String endArg)
    {
        this.source = source;
        this.dest = dest;
        this.startArg = startArg;
        this.endArg = endArg;
    }
    
    public String getSource()
    {
        return source;
    }
    
    public void setSource(String source)
    {
        this.source = source;
    }
    
    public String getDest()
    {
        return dest;
    }
    
    public void setDest(String dest)
    {
        this.dest = dest;
    }
    
    public String getStartArg()
    {
        return startArg;
    }
    
    public void setStartArg(String startArg)
    {
        this.startArg = startArg;
    }
    
    public String getEndArg()
    {
        return endArg;
    }
    
    public void setEndArg(String endArg)
    {
        this.endArg = endArg;
    }
}
