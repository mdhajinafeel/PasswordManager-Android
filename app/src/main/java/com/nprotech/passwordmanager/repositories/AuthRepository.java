package com.nprotech.passwordmanager.repositories;

import com.nprotech.passwordmanager.model.request.LoginRequest;
import com.nprotech.passwordmanager.model.response.LoginResponse;
import com.nprotech.passwordmanager.services.AuthApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class AuthRepository {

    private final AuthApiService authApiService;

    @Inject
    public AuthRepository(AuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    public Call<LoginResponse> authLogin(String email, String password) {
        return authApiService.authLogin(new LoginRequest(email, password));
    }
}