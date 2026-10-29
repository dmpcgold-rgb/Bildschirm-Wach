package at.screenawake.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeoutPolicyTest {
    @Test public void longTimeoutIsRecognized() {
        assertTrue(TimeoutPolicy.shouldRestore(Integer.MAX_VALUE));
        assertFalse(TimeoutPolicy.shouldRestore(30_000));
    }
    @Test public void manualNeverExpires() {
        assertEquals(0, TimeoutPolicy.expiresAt(100, 0));
        assertFalse(TimeoutPolicy.expired(10000, 0));
    }
    @Test public void twoHoursExpires() {
        long expiry = TimeoutPolicy.expiresAt(1000, 120);
        assertEquals(7_201_000L, expiry);
        assertFalse(TimeoutPolicy.expired(expiry - 1, expiry));
        assertTrue(TimeoutPolicy.expired(expiry, expiry));
    }
    @Test public void fourHoursExpires() {
        assertEquals(14_401_000L, TimeoutPolicy.expiresAt(1000, 240));
    }
}
