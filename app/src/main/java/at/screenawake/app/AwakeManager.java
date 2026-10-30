package at.screenawake.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.TileService;

public final class AwakeManager {
    private static final String PREFS = "awake_session_v1";
    private static final String ACTIVE = "active";
    private static final String OLD = "original_timeout";
    private static final String DEADLINE = "deadline_wall_clock";
    private static final String START = "started_wall_clock";
    private static final String DURATION = "duration_minutes";
    private static final int DEFAULT_TIMEOUT = 30000;
    private AwakeManager() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public static boolean canWrite(Context ctx) {
        return Settings.System.canWrite(ctx);
    }
    private static int timeout(Context ctx) {
        return Settings.System.getInt(ctx.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, DEFAULT_TIMEOUT);
    }
    public static int duration(Context ctx) {
        return prefs(ctx).getInt(DURATION, 0);
    }
    public static long startedAt(Context ctx) {
        return prefs(ctx).getLong(START, 0L);
    }
    public static long deadline(Context ctx) {
        return prefs(ctx).getLong(DEADLINE, 0L);
    }
    public static void setDuration(Context ctx, int value) {
        if (value != 0 && value != 120 && value != 240) throw new IllegalArgumentException();
        prefs(ctx).edit().putInt(DURATION, value).apply();
    }

    public static boolean isActive(Context ctx) {
        SharedPreferences p = prefs(ctx);
        if (!p.getBoolean(ACTIVE, false)) return false;
        if (!TimeoutPolicy.shouldRestore(timeout(ctx))) {
            clear(ctx);
            return false;
        }
        if (TimeoutPolicy.expired(System.currentTimeMillis(), p.getLong(DEADLINE, 0))) {
            deactivate(ctx);
            return prefs(ctx).getBoolean(ACTIVE, false);
        }
        return true;
    }

    public static boolean activate(Context ctx) {
        if (!canWrite(ctx)) return false;
        boolean oldActive = isActive(ctx);
        int old = oldActive ? prefs(ctx).getInt(OLD, DEFAULT_TIMEOUT) : timeout(ctx);
        long now = System.currentTimeMillis();
        long previousStart = oldActive ? prefs(ctx).getLong(START, 0L) : 0L;
        long started = previousStart > 0L ? previousStart : now;
        long deadline = TimeoutPolicy.expiresAt(now, duration(ctx));
        if (!prefs(ctx).edit().putInt(OLD, old).putLong(START, started)
                .putLong(DEADLINE, deadline).putBoolean(ACTIVE, true).commit()) return false;
        try {
            boolean ok = Settings.System.putInt(ctx.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, TimeoutPolicy.AWAKE_TIMEOUT_MS);
            if (!ok || !TimeoutPolicy.shouldRestore(timeout(ctx))) {
                rollback(ctx, old);
                return false;
            }
            schedule(ctx, deadline);
            refreshTile(ctx);
            return true;
        } catch (SecurityException | IllegalArgumentException ex) {
            rollback(ctx, old);
            return false;
        }
    }

    private static void rollback(Context ctx, int old) {
        if (canWrite(ctx) && TimeoutPolicy.shouldRestore(timeout(ctx))) {
            try {
                Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, old);
            } catch (SecurityException ignored) {}
        }
        clear(ctx);
    }

    public static boolean deactivate(Context ctx) {
        SharedPreferences p = prefs(ctx);
        if (!p.getBoolean(ACTIVE, false)) {
            cancelAlarm(ctx); refreshTile(ctx); return true;
        }
        if (TimeoutPolicy.shouldRestore(timeout(ctx))) {
            if (!canWrite(ctx)) return false;
            int old = p.getInt(OLD, DEFAULT_TIMEOUT);
            try {
                if (!Settings.System.putInt(ctx.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, old)
                    || timeout(ctx) != old) return false;
            } catch (SecurityException | IllegalArgumentException ex) {
                return false;
            }
        }
        clear(ctx);
        return true;
    }

    private static void clear(Context ctx) {
        prefs(ctx).edit().remove(ACTIVE).remove(OLD).remove(DEADLINE).remove(START).commit();
        cancelAlarm(ctx);
        refreshTile(ctx);
    }

    private static PendingIntent alarmIntent(Context ctx) {
        return PendingIntent.getBroadcast(ctx, 42,
            new Intent(ctx, TimeoutReceiver.class),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private static void cancelAlarm(Context ctx) {
        AlarmManager m = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (m != null) m.cancel(alarmIntent(ctx));
    }
    private static void schedule(Context ctx, long deadline) {
        cancelAlarm(ctx);
        if (deadline <= 0) return;
        AlarmManager m = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (m != null) m.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            deadline, alarmIntent(ctx));
    }
    public static void refreshTile(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            TileService.requestListeningState(ctx,
                new ComponentName(ctx, AwakeTileService.class));
    }
}
