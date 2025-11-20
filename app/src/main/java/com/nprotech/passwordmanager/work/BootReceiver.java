package com.nprotech.passwordmanager.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.utils.AppLogger;

/**
 * A {@link BroadcastReceiver} that listens for device boot completion and
 * re-schedules the hourly sync if it was previously enabled.
 * This ensures that the sync continues to run even after the device restarts.
 */
public class BootReceiver extends BroadcastReceiver {

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.
     * It checks for the {@link Intent#ACTION_BOOT_COMPLETED} action and
     * re-schedules the hourly sync if the sync interval is greater than 0.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AppLogger.d(getClass(), "⬇️ Device booted: rescheduling hourly sync");

            // Reschedule hourly sync
            if(PreferenceManager.INSTANCE.getSyncHours() > 0) {
                SyncScheduler.scheduleHourlySync(context, PreferenceManager.INSTANCE.getSyncHours());
            }
        }
    }
}
