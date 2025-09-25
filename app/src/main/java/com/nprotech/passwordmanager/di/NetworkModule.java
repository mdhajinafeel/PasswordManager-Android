package com.nprotech.passwordmanager.di;

import android.app.Application;

import com.nprotech.passwordmanager.utils.NetworkConnectivity;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    NetworkConnectivity provideNetworkConnectivity(Application application) {
        return new NetworkConnectivity(application);
    }
}