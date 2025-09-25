package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    private boolean isLoginStatus;
    private String message;

    public boolean isLoginStatus() {
        return isLoginStatus;
    }

    public void setLoginStatus(boolean loginStatus) {
        isLoginStatus = loginStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}