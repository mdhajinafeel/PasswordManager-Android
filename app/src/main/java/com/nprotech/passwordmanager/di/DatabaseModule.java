package com.nprotech.passwordmanager.di;

import android.content.Context;

import com.nprotech.passwordmanager.db.AppDatabase;
import com.nprotech.passwordmanager.db.dao.CategoryDao;
import com.nprotech.passwordmanager.db.dao.IconDao;
import com.nprotech.passwordmanager.db.dao.PasswordDao;
import com.nprotech.passwordmanager.db.dao.SchedulerDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt module for providing database-related dependencies.
 * <p>
 * This module is responsible for creating and providing singleton instances of the
 * {@link AppDatabase} and its associated DAOs.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    /**
     * Provides a singleton instance of the {@link AppDatabase}.
     *
     * @param context The application context.
     * @return A singleton instance of {@link AppDatabase}.
     */
    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    /**
     * Provides an instance of {@link CategoryDao}.
     *
     * @param db The application database.
     * @return An instance of {@link CategoryDao}.
     */
    @Provides
    public static CategoryDao provideCategoryDao(AppDatabase db) {
        return db.categoryDao();
    }

    /**
     * Provides an instance of {@link IconDao}.
     *
     * @param db The application database.
     * @return An instance of {@link IconDao}.
     */
    @Provides
    public static IconDao provideIconDao(AppDatabase db) {
        return db.iconDao();
    }

    /**
     * Provides an instance of {@link PasswordDao}.
     *
     * @param db The application database.
     * @return An instance of {@link PasswordDao}.
     */
    @Provides
    public static PasswordDao providePasswordDao(AppDatabase db) {
        return db.passwordDao();
    }

    /**
     * Provides an instance of {@link SchedulerDao}.
     *
     * @param db The application database.
     * @return An instance of {@link SchedulerDao}.
     */
    @Provides
    public static SchedulerDao provideSchedulerDao(AppDatabase db) {
        return db.schedulerDao();
    }
}