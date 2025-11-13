package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class DownloadMasterResponse implements Serializable {

    private boolean status;
    private String message;
    private DownloadMasterDataResponse data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public DownloadMasterDataResponse getData() {
        return data;
    }
}