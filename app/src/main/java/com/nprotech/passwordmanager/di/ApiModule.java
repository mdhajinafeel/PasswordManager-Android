package com.nprotech.passwordmanager.di;

import android.content.Context;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.services.IAuthApiService;
import com.nprotech.passwordmanager.services.IMasterApiService;
import com.nprotech.passwordmanager.services.IPasswordApiService;
import com.nprotech.passwordmanager.services.interceptors.BasicAuthInterceptor;
import com.nprotech.passwordmanager.services.interceptors.BearerTokenInterceptor;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Hilt module for providing API-related dependencies.
 * <p>
 * This module is responsible for creating and providing instances of Retrofit, OkHttpClient,
 * and the various API service interfaces used throughout the application.
 * It dynamically creates clients based on the user's login state to ensure the
 * correct authentication interceptor is used.
 */
@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    /**
     * Builds an {@link OkHttpClient} dynamically based on the current login state.
     * <p>
     * If the user is logged in, it uses a {@link BearerTokenInterceptor} to add the authorization token.
     * Otherwise, it uses a {@link BasicAuthInterceptor} for basic authentication.
     * This avoids caching a single client with an old token.
     *
     * @param context The application context.
     * @return A configured {@link OkHttpClient}.
     */
    private OkHttpClient buildDynamicOkHttpClient(@ApplicationContext Context context) {
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
     * Builds a {@link Retrofit} instance dynamically.
     * <p>
     * This ensures that if the token or login state changes, a new client with the
     * correct interceptor is always created.
     *
     * @param context The application context.
     * @return A configured {@link Retrofit} instance.
     */
    private Retrofit buildDynamicRetrofit(@ApplicationContext Context context) {
        AppLogger.d(getClass(), "buildDynamicRetrofit CALLED");
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(buildDynamicOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Provides an instance of {@link IAuthApiService}.
     *
     * @param context The application context.
     * @return An implementation of {@link IAuthApiService}.
     */
    @Provides
    public IAuthApiService provideAuthApiService(@ApplicationContext Context context) {
        AppLogger.d(getClass(), "provideAuthApiService CALLED");
        return buildDynamicRetrofit(context).create(IAuthApiService.class);
    }

    /**
     * Provides an instance of {@link IMasterApiService}.
     *
     * @param context The application context.
     * @return An implementation of {@link IMasterApiService}.
     */
    @Provides
    public IMasterApiService provideMasterApiService(@ApplicationContext Context context) {
        AppLogger.d(getClass(), "provideMasterApiService CALLED");
        return buildDynamicRetrofit(context).create(IMasterApiService.class);
    }

    /**
     * Provides an instance of {@link IPasswordApiService}.
     *
     * @param context The application context.
     * @return An implementation of {@link IPasswordApiService}.
     */
    @Provides
    public IPasswordApiService providePasswordApiService(@ApplicationContext Context context) {
        AppLogger.d(getClass(), "providePasswordApiService CALLED");
        return buildDynamicRetrofit(context).create(IPasswordApiService.class);
    }

    /**
     * Provides a singleton instance of {@link BearerTokenInterceptor}.
     * <p>
     * This is kept as a singleton and is intended for use where the background status is fixed.
     * Note: Hilt cannot provide the 'isBackground' boolean primitive directly.
     * This provider may need to be specialized (e.g., using @Named qualifiers) if different
     * interceptor instances are required.
     *
     * @param context      The application context.
     * @param isBackground A flag indicating if the interceptor is for a background task.
     * @return A singleton instance of {@link BearerTokenInterceptor}.
     */
    @Provides
    @Singleton
    public BearerTokenInterceptor provideBearerTokenInterceptor(@ApplicationContext Context context, boolean isBackground) {
        return new BearerTokenInterceptor(context, isBackground);
    }
}