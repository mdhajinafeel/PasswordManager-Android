package com.nprotech.passwordmanager.model.request;

import java.io.Serializable;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class LoginRequest implements Serializable {

    private String email, password, fcmToken, appVersion, deviceId;
    private boolean isGoogleLogin;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setGoogleLogin(boolean googleLogin) {
        isGoogleLogin = googleLogin;
    }
}