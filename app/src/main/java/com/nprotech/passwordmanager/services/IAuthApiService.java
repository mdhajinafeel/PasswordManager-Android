package com.nprotech.passwordmanager.services;

import com.nprotech.passwordmanager.constants.Urls;
import com.nprotech.passwordmanager.model.request.LoginRequest;
import com.nprotech.passwordmanager.model.request.RefreshTokenRequest;
import com.nprotech.passwordmanager.model.request.SignUpRequest;
import com.nprotech.passwordmanager.model.response.LoginResponse;
import com.nprotech.passwordmanager.model.response.LogoutResponse;
import com.nprotech.passwordmanager.model.response.RefreshTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IAuthApiService {

    @POST(Urls.authLogin)
    Call<LoginResponse> authLogin(@Body LoginRequest loginRequest);

    @POST(Urls.authRegister)
    Call<LoginResponse> authRegister(@Body SignUpRequest signUpRequest);

    @POST(Urls.authLogout)
    Call<LogoutResponse> authLogout();

    @POST(Urls.authRefreshToken)
    Call<RefreshTokenResponse> getRefreshTokenData(@Body RefreshTokenRequest refreshTokenRequest);
}