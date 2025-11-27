package com.nprotech.passwordmanager;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.security.KeyStore;
import java.util.List;

import dagger.hilt.android.HiltAndroidApp;
import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

@HiltAndroidApp
public class MyPasswordManagerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize preferences
        initSecureSharedPref();

        // Apply theme
        boolean darkMode = PreferenceManager.INSTANCE.getDarkMode();
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void initSecureSharedPref() {
        try {
            String prefName = "NPRO_PWD_MNGR";
            String prefix = "npro";
            byte[] seed = "npro".getBytes();
            SecuredPreferenceStore.init(getApplicationContext(), prefName, prefix, seed, new DefaultRecoveryHandler());
            SecuredPreferenceStore.setRecoveryHandler(new DefaultRecoveryHandler() {
                @Override
                protected boolean recover(Exception e, KeyStore keyStore, List<String> keyAliases, SharedPreferences preferences) {
                    return super.recover(e, keyStore, keyAliases, preferences);
                }
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error initSecureSharedPref", e);
        }
    }
}