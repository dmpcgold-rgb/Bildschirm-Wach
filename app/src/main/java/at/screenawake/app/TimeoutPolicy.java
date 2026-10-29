package at.screenawake.app;

public final class TimeoutPolicy {
    private TimeoutPolicy() {}
    public static final int AWAKE_TIMEOUT_MS = Integer.MAX_VALUE;
    public static boolean shouldRestore(int timeout) {
        return timeout == AWAKE_TIMEOUT_MS;
    }
    public static long expiresAt(long now, int minutes) {
        return minutes <= 0 ? 0 : now + minutes * 60_000L;
    }
    public static boolean expired(long now, long deadline) {
        return deadline > 0 && now >= deadline;
    }
}
