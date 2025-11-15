package com.nprotech.passwordmanager.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.utils.AppLogger;

public class BootReceiver extends BroadcastReceiver {

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