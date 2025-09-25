package com.nprotech.passwordmanager.services;

import com.nprotech.passwordmanager.constants.Urls;
import com.nprotech.passwordmanager.model.request.LoginRequest;
import com.nprotech.passwordmanager.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST(Urls.authUrl)
    Call<LoginResponse> authLogin(@Body LoginRequest loginRequest);
}