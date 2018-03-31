package dark.ash.com.soulmusicplayer.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by hp on 24-03-2018.
 */

public class TimeUtils {


    public static String longToTime(long time) {
        long min = TimeUnit.MILLISECONDS.toMinutes(time);
        long sec = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        String dateString = min + ":" + sec;
        return dateString;
    }
}
