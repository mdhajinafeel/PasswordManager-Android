package com.nprotech.passwordmanager.utils;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Base64;

public class CommonUtils {

    //API Constants
    public static final int masterApiId = 1;
    public static final String masterApiName = "Download Master";
    public static final int syncApiId = 2;
    public static final String syncApiName = "Sync Data";

    //Password Priority
    public static final int passwordWeak = 1;
    public static final int passwordMedium = 2;
    public static final int passwordStrong = 3;
    public static final int passwordVeryStrong = 4;

    public static String generateNonce(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] nonceBytes = new byte[length];
        secureRandom.nextBytes(nonceBytes);

        // Use android.util.Base64 to support all SDK versions
        return Base64.encodeToString(nonceBytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.e(context.getClass(), "getAppVersionName", e);
            return "";
        }
    }
    public static long getAppVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.e(context.getClass(), "getAppVersionCode", e);
            return -1;
        }
    }
    public static SpannableString customFontTypeFace(Typeface typeface, CharSequence chars) {
        if (chars == null) {
            return null;
        }
        SpannableString s = new SpannableString(chars);
        s.setSpan(new CustomTypefaceSpan("", typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public static boolean getLoginExpiry(long loginExpiry) {
        boolean timeStatus = false;
        if (loginExpiry > 0) {
            long currentTimeInMilliseconds = System.currentTimeMillis();
            long differenceBetweenTimes = loginExpiry - currentTimeInMilliseconds;
            timeStatus = differenceBetweenTimes <= 0;
        }
        return timeStatus;
    }

    public static Long getCurrentDateTimeStamp(boolean isRandomRequired) {
        long timestamp = System.currentTimeMillis();

        if (isRandomRequired) {
            long random = ThreadLocalRandom.current().nextLong(1000, 9999); // 4-digit
            return Long.parseLong(random + "" + timestamp); // Prefix + timestamp
        }

        return timestamp;
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return  day + "_" + month + "_" + year;
    }
}