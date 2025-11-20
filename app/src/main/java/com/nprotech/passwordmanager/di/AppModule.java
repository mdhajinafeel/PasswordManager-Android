package com.nprotech.passwordmanager.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module that provides application-level dependencies.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    /**
     * Provides the application context.
     *
     * @param application The application instance.
     * @return The application context.
     */
    @Provides
    @Singleton
    Context provideApplicationContext(Application application) {
        return application.getApplicationContext();
    }
}
