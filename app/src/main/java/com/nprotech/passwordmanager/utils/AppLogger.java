package com.nprotech.passwordmanager.utils;

import android.util.Log;

@SuppressWarnings("unused")
public class AppLogger {

    public static void d(Class<?> cls, String message) {
        Log.d(cls.getSimpleName(), message);
    }

    public static void i(Class<?> cls, String message) {
        Log.i(cls.getSimpleName(), message);
    }

    public static void w(Class<?> cls, String message) {
        Log.w(cls.getSimpleName(), message);
    }

    public static void e(Class<?> cls, String message, Throwable t) {
        Log.e(cls.getSimpleName(), message, t);
    }
}