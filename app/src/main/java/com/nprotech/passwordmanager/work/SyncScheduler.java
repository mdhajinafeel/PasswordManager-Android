package com.nprotech.passwordmanager.work;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.concurrent.TimeUnit;

public class SyncScheduler {

    private static final int SYNC_REQUEST_CODE = 1001;

    /** Schedule exact hourly sync */
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
        SyncWorker.enqueuePeriodicWork(context);
    }

    /** Cancel hourly sync */
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
    }
}