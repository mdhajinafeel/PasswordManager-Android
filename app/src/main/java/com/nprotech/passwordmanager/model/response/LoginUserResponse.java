package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

public class LoginUserResponse implements Serializable {

    private String name, email, secretKey;
    private boolean isGoogle;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isGoogle() {
        return isGoogle;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setGoogle(boolean google) {
        isGoogle = google;
    }
}