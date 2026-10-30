package at.screenawake.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SessionClockTest {
    @Test public void remainingShowsTwoHoursAtStart() {
        assertEquals("02:00:00", SessionClock.remaining(1_000L, 7_201_000L));
    }

    @Test public void remainingRoundsUpUntilDeadline() {
        assertEquals("00:00:02", SessionClock.remaining(1_001L, 3_000L));
        assertEquals("00:00:01", SessionClock.remaining(2_999L, 3_000L));
        assertEquals("00:00:00", SessionClock.remaining(3_000L, 3_000L));
        assertEquals("00:00:00", SessionClock.remaining(4_000L, 3_000L));
    }

    @Test public void elapsedShowsHoursMinutesSeconds() {
        assertEquals("01:02:03", SessionClock.elapsed(3_724_000L, 1_000L));
        assertEquals("00:00:00", SessionClock.elapsed(1_000L, 0L));
        assertEquals("00:00:00", SessionClock.elapsed(1_000L, 2_000L));
    }

    @Test public void elapsedSupportsMoreThanTwentyFourHours() {
        assertEquals("27:00:00", SessionClock.elapsed(97_201_000L, 1_000L));
    }
}
