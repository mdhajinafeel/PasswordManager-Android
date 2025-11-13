package com.nprotech.passwordmanager.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.concurrent.TimeUnit;

public class WorkScheduler {

    private static final String WORK_TAG = "AutoSyncWorker";

    /**
     * Schedule a sync worker: runs once immediately, then periodically every 'hours'.
     */
    public static void scheduleSyncWork(Context context, int hours) {

        WorkManager workManager = WorkManager.getInstance(context);

        // Cancel any existing scheduled work to avoid duplicates
        cancelSyncWork(context);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 1️⃣ Enqueue one-time work immediately
        OneTimeWorkRequest immediateWork = new OneTimeWorkRequest.Builder(ApiWorker.class)
                .addTag(WORK_TAG)
                .setConstraints(constraints)
                .build();

        workManager.enqueue(immediateWork);
        AppLogger.d(WorkScheduler.class, "✅ Scheduled immediate sync");

        // 2️⃣ Schedule periodic work for future runs
        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(ApiWorker.class, hours, TimeUnit.HOURS)
                .addTag(WORK_TAG)
                .setConstraints(constraints)
                .setInitialDelay(hours, TimeUnit.HOURS) // avoid immediate run
                .build();

        workManager.enqueue(periodicWork);
        AppLogger.d(WorkScheduler.class, "✅ Scheduled periodic sync every " + hours + " hours");

        // Optional: observe state changes
        workManager.getWorkInfoByIdLiveData(periodicWork.getId())
                .observeForever(workInfo -> {
                    if (workInfo != null) {
                        AppLogger.d(WorkScheduler.class, "📡 Worker state: " + workInfo.getState().name());
                    }
                });
    }

    public static void cancelSyncWork(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(WORK_TAG);
        AppLogger.d(WorkScheduler.class, "🛑 All sync work cancelled");
    }
}