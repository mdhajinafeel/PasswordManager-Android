package com.nprotech.passwordmanager.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.db.AppDatabase;
import com.nprotech.passwordmanager.repositories.MasterRepository;
import com.nprotech.passwordmanager.services.IMasterApiService;
import com.nprotech.passwordmanager.services.interceptors.BearerTokenInterceptor;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A {@link Worker} that performs periodic synchronization of master data.
 * This worker is responsible for downloading the latest data from the server
 * and updating the local database. It can be scheduled for immediate or
 * periodic execution.
 */
public class SyncWorker extends Worker {

    private static final String UNIQUE_WORK_NAME = "MasterSyncWorker";

    /**
     * Constructs a new SyncWorker.
     *
     * @param context The application context.
     * @param params  Parameters to setup the worker.
     */
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * The main entry point for the worker. This method is called by WorkManager to
     * perform the sync operation.
     *
     * @return The result of the work, either {@link Result#success()} or {@link Result#retry()}.
     */
    @NonNull
    @Override
    public Result doWork() {
        AppLogger.d(getClass(), "⬇️ Performing periodic sync");

        try {
            IMasterApiService apiService = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(new OkHttpClient.Builder()
                            .connectTimeout(120, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS)
                            .addInterceptor(new BearerTokenInterceptor(getApplicationContext(), true))
                            .build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(IMasterApiService.class);

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            MasterRepository masterRepository = new MasterRepository(apiService, db.categoryDao(), db.iconDao(), db.schedulerDao(), db.passwordDao());

            masterRepository.masterDownload();
            AppLogger.d(getClass(), "✅ Sync successful");

            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "❌ Sync failed", e);
            return Result.retry();
        }
    }

    /**
     * Enqueues an immediate sync operation to be run in a background thread.
     * This method is useful for triggering a sync on-demand.
     * It also clears the scheduler data before starting the sync.
     *
     * @param context The application context.
     */
    public static void enqueueImmediateSync(Context context) {
        new Thread(() -> {
            try {
                IMasterApiService apiService = new Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .client(new OkHttpClient.Builder()
                                .connectTimeout(120, TimeUnit.SECONDS)
                                .readTimeout(120, TimeUnit.SECONDS)
                                .addInterceptor(new BearerTokenInterceptor(context, true))
                                .build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(IMasterApiService.class);

                AppDatabase db = AppDatabase.getInstance(context);
                MasterRepository masterRepository = new MasterRepository(apiService,
                        db.categoryDao(), db.iconDao(), db.schedulerDao(), db.passwordDao());

                AppLogger.d(context.getClass(), "⬇️ Clearing Scheduler Data");
                db.schedulerDao().clearAll();
                AppLogger.d(context.getClass(), "✅ Cleared Successfully");

                masterRepository.masterDownload();
                AppLogger.d(context.getClass(), "✅ Sync successful (Immediate)");
            } catch (Exception e) {
                AppLogger.e(context.getClass(), "❌ Sync failed (Immediate)", e);
            }
        }).start();
    }

    /**
     * Enqueues a periodic work request using WorkManager as a fallback for devices
     * with aggressive power-saving features.
     * The work is scheduled with a network constraint.
     *
     * @param context The application context.
     * @param hours   The interval in hours at which to repeat the sync.
     */
    public static void enqueuePeriodicWork(Context context, int hours) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, hours, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}
