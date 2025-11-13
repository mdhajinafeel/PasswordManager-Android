package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
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
}