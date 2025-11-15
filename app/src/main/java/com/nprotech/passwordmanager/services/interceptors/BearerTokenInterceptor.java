package com.nprotech.passwordmanager.services.interceptors;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.model.request.RefreshTokenRequest;
import com.nprotech.passwordmanager.model.response.RefreshTokenResponse;
import com.nprotech.passwordmanager.services.IAuthApiService;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.view.activities.LoginActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BearerTokenInterceptor implements Interceptor {

    private final Context context;
    private final boolean isBackground;

    @Inject
    public BearerTokenInterceptor(Context context, boolean isBackground) {
        this.context = context.getApplicationContext();
        this.isBackground = isBackground;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        // 🔹 Always get latest token dynamically
        String accessToken = PreferenceManager.INSTANCE.getAccessToken();

        // No token? → proceed without Bearer header (BasicAuth used instead)
        if (accessToken.isEmpty()) {
            return chain.proceed(request);
        }

        boolean loginExpired = CommonUtils.getLoginExpiry(PreferenceManager.INSTANCE.getLoginExpiry());

        if (!loginExpired) {
            // Token still valid
            Request modifiedRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            return chain.proceed(modifiedRequest);
        } else {
            // Token expired → refresh it
            RefreshTokenResponse refreshModel = refreshAccessTokenServiceHit();
            if (refreshModel != null && refreshModel.getAccessToken() != null) {
                Request modifiedRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + PreferenceManager.INSTANCE.getAccessToken())
                        .build();
                return chain.proceed(modifiedRequest);
            } else {
                handleLogout();
            }
        }

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body(ResponseBody.create("Unauthorized", MediaType.get("text/plain; charset=utf-8")))
                .build();
    }

    private RefreshTokenResponse refreshAccessTokenServiceHit() {
        RefreshTokenResponse model = null;
        try {
            IAuthApiService authApiService = getAccessTokenRefresh();
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken(PreferenceManager.INSTANCE.getRefreshToken());
            Call<RefreshTokenResponse> call = authApiService.getRefreshTokenData(req);
            model = call.execute().body();

            if (model != null && model.getAccessToken() != null) {
                PreferenceManager.INSTANCE.setAccessToken(model.getAccessToken());
                PreferenceManager.INSTANCE.setRefreshToken(model.getRefreshToken());
                PreferenceManager.INSTANCE.setLoginExpiry(model.getLoginExpiresAt());
                PreferenceManager.INSTANCE.setRefreshTokenExpiry(model.getRefreshExpiresAt());
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                AppLogger.e(getClass(), "Error refreshing token", e);
            }
        }
        return model;
    }

    private static IAuthApiService getAccessTokenRefresh() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(BuildConfig.BASIC_AUTH_CLIENT, BuildConfig.BASIC_AUTH_PASSWORD))
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(IAuthApiService.class);
    }

    private void handleLogout() {
        if (!isBackground) {
            PreferenceManager.INSTANCE.clearLoginSession();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            AppLogger.d(getClass(), "Background request: token expired, skipping login redirect");
        }

    }
}