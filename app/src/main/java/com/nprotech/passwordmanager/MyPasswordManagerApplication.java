package com.nprotech.passwordmanager;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.nprotech.passwordmanager.helper.PreferenceManager;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyPasswordManagerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager.INSTANCE.init(this);

        // Apply theme
        boolean darkMode = PreferenceManager.INSTANCE.getDarkMode();
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}