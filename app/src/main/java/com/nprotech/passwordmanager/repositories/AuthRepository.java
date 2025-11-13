package com.nprotech.passwordmanager.repositories;

import com.nprotech.passwordmanager.model.request.LoginRequest;
import com.nprotech.passwordmanager.model.request.SignUpRequest;
import com.nprotech.passwordmanager.model.response.LoginResponse;
import com.nprotech.passwordmanager.model.response.LogoutResponse;
import com.nprotech.passwordmanager.services.IAuthApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class AuthRepository {

    private final IAuthApiService authApiService;

    @Inject
    public AuthRepository(IAuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    public Call<LoginResponse> authLogin(LoginRequest loginRequest) {
        return authApiService.authLogin(loginRequest);
    }

    public Call<LoginResponse> authRegister(SignUpRequest signUpRequest) {
        return authApiService.authRegister(signUpRequest);
    }

    public Call<LogoutResponse> authLogout() {
        return authApiService.authLogout();
    }
}