package com.nprotech.passwordmanager.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.nprotech.passwordmanager.utils.AppLogger;

@SuppressWarnings({"deprecation", "unused"})
public enum PreferenceManager {

    INSTANCE;

    private static final String PREF_NAME = "NPRO_PWD_MNGR_PREF";
    private static final String KEY_LOGINEXPIRY = "loginExpiry";
    private static final String KEY_ACCESSTOKEN = "accessToken";
    private static final String KEY_REFRESHTOKEN = "refreshToken";
    private static final String KEY_REFRESHTOKEN_EXPIRY = "refreshTokenExpiry";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL_ID = "emailId";
    private static final String KEY_GOOGLE_SIGN_IN = "googleSignIn";
    private static final String KEY_LOGGED_IN = "loggedIn";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_BIOMETRIC = "bioMetric";
    private static final String KEY_SECRET = "secretKey";
    private static final String KEY_SYNC_ID = "syncId";
    private static final String KEY_SYNC_HOURS = "syncHours";
    private SharedPreferences prefStore;

    // Initialize once in Application or Activity
    public void init(Context context) {
        try {
            // ✅ API 23+ use EncryptedSharedPreferences
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefStore = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "PreferenceManager Init", e);
            // Fallback to normal SharedPreferences if encrypted fails
            prefStore = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    // ===== Access Token =====
    public void setAccessToken(String token) {
        prefStore.edit().putString(KEY_ACCESSTOKEN, token).apply();
    }

    public String getAccessToken() {
        return prefStore.getString(KEY_ACCESSTOKEN, "");
    }

    // ===== Refresh Token =====
    public void setRefreshToken(String token) {
        prefStore.edit().putString(KEY_REFRESHTOKEN, token).apply();
    }

    public String getRefreshToken() {
        return prefStore.getString(KEY_REFRESHTOKEN, "");
    }

    public void setRefreshTokenExpiry(Long expiry) {
        prefStore.edit().putLong(KEY_REFRESHTOKEN_EXPIRY, expiry).apply();
    }

    public long getRefreshTokenExpiry() {
        return prefStore.getLong(KEY_REFRESHTOKEN_EXPIRY, 0);
    }

    // ===== Login Expiry =====
    public void setLoginExpiry(Long expiry) {
        prefStore.edit().putLong(KEY_LOGINEXPIRY, expiry).apply();
    }

    public long getLoginExpiry() {
        return prefStore.getLong(KEY_LOGINEXPIRY, 0);
    }

    // ===== Name =====
    public void setName(String name) {
        prefStore.edit().putString(KEY_NAME, name).apply();
    }

    public String getName() {
        return prefStore.getString(KEY_NAME, "");
    }

    // ===== Email =====
    public void setEmail(String email) {
        prefStore.edit().putString(KEY_EMAIL_ID, email).apply();
    }

    public String getEmail() {
        return prefStore.getString(KEY_EMAIL_ID, "");
    }

    // ===== LoggedIn =====
    public void setLoggedIn(boolean loggedIn) {
        prefStore.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public boolean getLoggedIn() {
        return prefStore.getBoolean(KEY_LOGGED_IN, false);
    }

    // ===== GoogleSignIn =====
    public void setGoogleSignIn(boolean googleSignIn) {
        prefStore.edit().putBoolean(KEY_GOOGLE_SIGN_IN, googleSignIn).apply();
    }

    public boolean getGoogleSignIn() {
        return prefStore.getBoolean(KEY_GOOGLE_SIGN_IN, false);
    }

    // ===== DarkMode =====
    public void setDarkMode(boolean darkMode) {
        prefStore.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    public boolean getDarkMode() {
        return prefStore.getBoolean(KEY_DARK_MODE, false);
    }

    // ===== Biometric =====
    public void setBioMetric(boolean bioMetric) {
        prefStore.edit().putBoolean(KEY_BIOMETRIC, bioMetric).apply();
    }

    public boolean getBioMetric() {
        return prefStore.getBoolean(KEY_BIOMETRIC, false);
    }

    // ===== Secret Key =====
    public void setSecretKey(String secretKey) {
        prefStore.edit().putString(KEY_SECRET, secretKey).apply();
    }

    public String getSecretKey() {
        return prefStore.getString(KEY_SECRET, "");
    }

    // ===== Sync Id =====
    public void setSyncId(int syncId) {
        prefStore.edit().putInt(KEY_SYNC_ID, syncId).apply();
    }

    public int getSyncId() {
        return prefStore.getInt(KEY_SYNC_ID, 0);
    }

    // ===== Sync Hours =====
    public void setSyncHours(int syncHours) {
        prefStore.edit().putInt(KEY_SYNC_HOURS, syncHours).apply();
    }

    public int getSyncHours() {
        return prefStore.getInt(KEY_SYNC_HOURS, 0);
    }

    // Optional: clear all
    public void clearAll() {
        prefStore.edit().clear().apply();
    }

    public void clearLoginSession() {
        prefStore.edit()
                .remove(KEY_ACCESSTOKEN)
                .remove(KEY_REFRESHTOKEN)
                .remove(KEY_REFRESHTOKEN_EXPIRY)
                .remove(KEY_LOGINEXPIRY)
                .remove(KEY_LOGGED_IN)
                .remove(KEY_NAME)
                .remove(KEY_EMAIL_ID)
                .remove(KEY_GOOGLE_SIGN_IN)
                .remove(KEY_BIOMETRIC)
                .remove(KEY_SECRET)
                .remove(KEY_DARK_MODE)
                .remove(KEY_SYNC_ID)
                .remove(KEY_SYNC_HOURS)
                .apply();
    }
}