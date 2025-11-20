package com.nprotech.passwordmanager.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.utils.AppLogger;

/**
 * A {@link BroadcastReceiver} that listens for alarms to trigger a sync operation.
 * This receiver is responsible for starting the {@link SyncWorker} to perform the
 * actual sync and then rescheduling the next alarm.
 */
public class SyncReceiver extends BroadcastReceiver {

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.
     * It triggers an immediate sync using {@link SyncWorker#enqueueImmediateSync(Context)}
     * and reschedules the next hourly sync.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        AppLogger.d(getClass(), "⬇️ Alarm triggered: performing sync");

        // Call your API sync
        SyncWorker.enqueueImmediateSync(context);

        // Reschedule next alarm
        if(PreferenceManager.INSTANCE.getSyncHours() > 0) {
            SyncScheduler.scheduleHourlySync(context, PreferenceManager.INSTANCE.getSyncHours());
        }
    }
}