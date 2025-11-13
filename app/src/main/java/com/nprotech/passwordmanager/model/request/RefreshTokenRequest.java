package com.nprotech.passwordmanager.model.request;

import java.io.Serializable;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class RefreshTokenRequest implements Serializable {

    private String refreshToken;

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}