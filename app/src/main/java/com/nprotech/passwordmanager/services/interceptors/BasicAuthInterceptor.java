package com.nprotech.passwordmanager.services.interceptors;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthInterceptor implements Interceptor {

    private final String credentials;

    public BasicAuthInterceptor(String username, String password) {
        String auth = username + ":" + password;
        this.credentials = "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials)
                .build();
        return chain.proceed(authenticatedRequest);
    }
}