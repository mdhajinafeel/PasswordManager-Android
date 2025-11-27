package com.nprotech.passwordmanager.services;

import com.nprotech.passwordmanager.constants.Urls;
import com.nprotech.passwordmanager.model.request.FavouriteRequest;
import com.nprotech.passwordmanager.model.request.PasswordRequest;
import com.nprotech.passwordmanager.model.response.SavePasswordResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IPasswordApiService {

    @POST(Urls.savePassword)
    Call<SavePasswordResponse> savePassword(@Body PasswordRequest passwordRequest);

    @POST(Urls.favouritePassword)
    Call<SavePasswordResponse> favouritePassword(@Body FavouriteRequest favouriteRequest);
}