package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class RefreshTokenResponse implements Serializable {

    private String accessToken, refreshToken;
    private long loginExpiresAt, refreshExpiresAt;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getLoginExpiresAt() {
        return loginExpiresAt;
    }

    public void setLoginExpiresAt(long loginExpiresAt) {
        this.loginExpiresAt = loginExpiresAt;
    }

    public long getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public void setRefreshExpiresAt(long refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }
}