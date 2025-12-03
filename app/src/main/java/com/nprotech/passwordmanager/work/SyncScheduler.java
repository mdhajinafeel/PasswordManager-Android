package com.nprotech.passwordmanager.work;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.concurrent.TimeUnit;

/**
 * Schedules periodic sync operations using {@link AlarmManager} and {@link SyncWorker}.
 * This class provides methods to schedule and cancel hourly syncs.
 * The sync is scheduled using an exact alarm for precision, with a fallback
 * to {@link androidx.work.WorkManager} for devices with aggressive power-saving features.
 */
public class SyncScheduler {

    private static final int SYNC_REQUEST_CODE = 1001;

    /**
     * Schedules an exact hourly sync.
     *
     * @param context The application context.
     * @param hours   The interval in hours at which to repeat the sync.
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void scheduleHourlySync(Context context, int hours) {
        long triggerAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                SYNC_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
        }

        AppLogger.d(context.getClass(), "⏰ Next hourly sync scheduled at: " + triggerAt);

        // Schedule fallback using WorkManager for aggressive devices
        SyncWorker.enqueuePeriodicWork(context, hours);
    }

    /**
     * Cancels the hourly sync.
     *
     * @param context The application context.
     */
    public static void cancelHourlySync(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                SYNC_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) alarmManager.cancel(pendingIntent);

        AppLogger.d(context.getClass(), "❌ Scheduled cancelled");
    }
}
