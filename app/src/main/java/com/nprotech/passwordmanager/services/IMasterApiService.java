package com.nprotech.passwordmanager.services;

import com.nprotech.passwordmanager.constants.Urls;
import com.nprotech.passwordmanager.model.response.DownloadMasterResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IMasterApiService {

    @GET(Urls.masterDownload)
    Call<DownloadMasterResponse> masterDownload();
}