package io.github.trdesilva.autorecorder;

import java.util.concurrent.TimeUnit;

public class TimestampUtil
{
    public static String formatTime(long millis)
    {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis % TimeUnit.HOURS.toMillis(1));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis % TimeUnit.MINUTES.toMillis(1));
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
    
    public static long parseTime(String timestamp)
    {
        String[] numbers = timestamp.split(":");
        
        if(numbers.length == 3)
        {
            long millis = TimeUnit.SECONDS.toMillis(Long.parseLong(numbers[2]));
            millis += TimeUnit.MINUTES.toMillis(Long.parseLong(numbers[1]));
            millis += TimeUnit.HOURS.toMillis(Long.parseLong(numbers[0]));
            
            return millis;
        }
        return -1;
    }
}
