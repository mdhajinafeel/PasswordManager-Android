package com.nprotech.passwordmanager.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.db.AppDatabase;
import com.nprotech.passwordmanager.db.entities.SchedulerEntity;
import com.nprotech.passwordmanager.repositories.MasterRepository;
import com.nprotech.passwordmanager.services.IMasterApiService;
import com.nprotech.passwordmanager.services.interceptors.BearerTokenInterceptor;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiWorker extends Worker {

    public ApiWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppLogger.d(getClass(), "🚀 ApiWorker started");

        try {
            // 1️⃣ Setup API service
            IMasterApiService apiService = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).client(buildOkHttpClient()).addConverterFactory(GsonConverterFactory.create()).build().create(IMasterApiService.class);

            // 2️⃣ Initialize database DAOs
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            MasterRepository masterRepository = new MasterRepository(apiService, db.categoryDao(), db.iconDao(), db.schedulerDao());

            // 3️⃣ Call master download
            AppLogger.d(getClass(), "⬇️ Downloading master data...");
            masterRepository.masterDownload();
            AppLogger.d(getClass(), "✅ Master data downloaded successfully");

            // 4️⃣ Return success, no UI changes here
            return Result.success();

        } catch (Exception e) {
            AppLogger.e(getClass(), "❌ ApiWorker failed", e);
            // Retry in case of network or API failure
            return Result.retry();
        }
    }

    private OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().connectTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS);

        // Add BearerTokenInterceptor, but avoid any UI logic in background
        httpClient.addInterceptor(new BearerTokenInterceptor(getApplicationContext(), true));

        return httpClient.build();
    }
}