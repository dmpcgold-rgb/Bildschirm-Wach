package at.screenawake.app;

import java.util.Locale;

/** Format elapsed and remaining screen-awake session time independently of Android UI. */
public final class SessionClock {
    private SessionClock() {}

    public static String elapsed(long now, long startedAt) {
        return formatSeconds(startedAt <= 0L ? 0L : Math.max(0L, now - startedAt) / 1000L);
    }

    public static String remaining(long now, long deadline) {
        if (deadline <= now) return formatSeconds(0L);
        long millis = deadline - now;
        // Round up, so the display never reaches 00:00:00 before the deadline.
        long seconds = millis / 1000L + (millis % 1000L == 0L ? 0L : 1L);
        return formatSeconds(seconds);
    }

    private static String formatSeconds(long seconds) {
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, secs);
    }
}
