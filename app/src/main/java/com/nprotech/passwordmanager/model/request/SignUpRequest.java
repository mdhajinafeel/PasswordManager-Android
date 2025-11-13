package com.nprotech.passwordmanager.model.request;

import java.io.Serializable;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class SignUpRequest implements Serializable {

    private String name, email, password, fcmToken, appVersion, deviceId;
    private boolean isGoogle;

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGoogle(boolean google) {
        isGoogle = google;
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
}