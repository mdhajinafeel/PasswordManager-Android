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

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public static CategoryDao provideCategoryDao(AppDatabase db) {
        return db.categoryDao();
    }

    @Provides
    public static IconDao provideIconDao(AppDatabase db) {
        return db.iconDao();
    }

    @Provides
    public static PasswordDao providePasswordDao(AppDatabase db) {
        return db.passwordDao();
    }

    @Provides
    public static SchedulerDao provideSchedulerDao(AppDatabase db) {
        return db.schedulerDao();
    }
}