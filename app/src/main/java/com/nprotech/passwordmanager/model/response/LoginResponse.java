package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class LoginResponse implements Serializable {

    private boolean status;
    private String message, accessToken, refreshToken;
    private long loginExpiresAt, refreshExpiresAt;
    private LoginUserResponse user;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getLoginExpiresAt() {
        return loginExpiresAt;
    }

    public long getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public LoginUserResponse getUser() {
        return user;
    }
}