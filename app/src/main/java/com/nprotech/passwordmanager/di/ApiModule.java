package com.nprotech.passwordmanager.di;

import com.nprotech.passwordmanager.BuildConfig;
import com.nprotech.passwordmanager.services.AuthApiService;
import com.nprotech.passwordmanager.services.interceptors.BasicAuthInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    // Provide OkHttpClient with Basic Auth
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        // Replace with your actual username and password
        return new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor("yourUsername", "yourPassword"))
                .build();
    }

    // Provide Retrofit instance
    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Provide Api Services
    @Provides
    @Singleton
    public AuthApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(AuthApiService.class);
    }
}