package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class LogoutResponse implements Serializable {

    private String message;
    private boolean status;

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }
}