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

public class SyncWorker extends Worker {

    private static final String UNIQUE_WORK_NAME = "MasterSyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

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
            MasterRepository masterRepository = new MasterRepository(apiService, db.categoryDao(), db.iconDao(), db.schedulerDao());

            masterRepository.masterDownload();
            AppLogger.d(getClass(), "✅ Sync successful");

            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "❌ Sync failed", e);
            return Result.retry();
        }
    }

    /**
     * Run API sync immediately in background thread
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
                        db.categoryDao(), db.iconDao(), db.schedulerDao());

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
     * WorkManager periodic fallback for aggressive devices
     */
    public static void enqueuePeriodicWork(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}