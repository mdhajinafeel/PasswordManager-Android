package com.nprotech.passwordmanager.di;

import android.content.Context;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.services.IAuthApiService;
import com.nprotech.passwordmanager.services.IMasterApiService;
import com.nprotech.passwordmanager.services.interceptors.BasicAuthInterceptor;
import com.nprotech.passwordmanager.services.interceptors.BearerTokenInterceptor;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    /**
     * Instead of caching a single OkHttpClient forever (which keeps old token),
     * we will dynamically build it based on current login state.
     */
    private OkHttpClient buildDynamicOkHttpClient(Context context) {
        AppLogger.d(getClass(), "buildDynamicOkHttpClient CALLED");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS);

        if (PreferenceManager.INSTANCE.getLoggedIn()) {
            AppLogger.d(getClass(), "Using BearerTokenInterceptor");
            httpClient.addInterceptor(new BearerTokenInterceptor(context, false));
        } else {
            AppLogger.d(getClass(), "Using BasicAuthInterceptor");
            httpClient.addInterceptor(new BasicAuthInterceptor(
                    BuildConfig.BASIC_AUTH_CLIENT,
                    BuildConfig.BASIC_AUTH_PASSWORD
            ));
        }

        return httpClient.build();
    }

    /**
     * Instead of making Retrofit @Singleton, we will rebuild it dynamically.
     * This ensures that if token or login state changes, we always get the correct client.
     */
    private Retrofit buildDynamicRetrofit(Context context) {
        AppLogger.d(getClass(), "buildDynamicRetrofit CALLED");
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(buildDynamicOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // --- Provide API Services using dynamic Retrofit ---

    @Provides
    public IAuthApiService provideAuthApiService(Context context) {
        AppLogger.d(getClass(), "provideAuthApiService CALLED");
        return buildDynamicRetrofit(context).create(IAuthApiService.class);
    }

    @Provides
    public IMasterApiService provideMasterApiService(Context context) {
        AppLogger.d(getClass(), "provideMasterApiService CALLED");
        return buildDynamicRetrofit(context).create(IMasterApiService.class);
    }

    // --- Keep this as Singleton since token reading uses it ---
    @Provides
    @Singleton
    public BearerTokenInterceptor provideBearerTokenInterceptor(Context context, boolean isBackground) {
        return new BearerTokenInterceptor(context, isBackground);
    }
}