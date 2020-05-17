package JavKing.util;

import java.time.Duration;
import java.time.LocalTime;

public class TimeUtil {

    public static String millisecondsToHHMMSS(long milliseconds) {
        long hours = Duration.ofMillis(milliseconds).toHours();
        long minutes = Duration.ofMillis(milliseconds).toMinutes() - hours * 60;
        long seconds = Duration.ofMillis(milliseconds).toSeconds() - hours * 3600 - minutes * 60;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static String secondsToHHMMSS(long seconds) {
        return millisecondsToHHMMSS(seconds * 1000L);
    }

    public static long HHMMSStoMilliseconds(String hms) {
        return HHMMSStoSeconds(hms) * 1000;
    }

    public static long HHMMSStoSeconds(String hms) {
        LocalTime localDateTime = LocalTime.parse(hms);
        return localDateTime.toSecondOfDay();
    }
}
