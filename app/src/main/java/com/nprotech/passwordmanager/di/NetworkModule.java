package com.nprotech.passwordmanager.di;

import android.app.Application;

import com.nprotech.passwordmanager.utils.NetworkConnectivity;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module for providing network-related dependencies.
 * <p>
 * This module is responsible for providing network connectivity information
 * to other parts of the application.
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    /**
     * Provides an instance of {@link NetworkConnectivity}.
     *
     * @param application The application instance.
     * @return An instance of {@link NetworkConnectivity}.
     */
    @Provides
    NetworkConnectivity provideNetworkConnectivity(Application application) {
        return new NetworkConnectivity(application);
    }
}