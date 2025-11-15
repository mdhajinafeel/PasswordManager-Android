package com.nprotech.passwordmanager.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.utils.AppLogger;

public class SyncReceiver extends BroadcastReceiver {

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