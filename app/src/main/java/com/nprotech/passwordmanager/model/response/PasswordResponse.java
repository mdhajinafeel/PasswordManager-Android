package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class PasswordResponse implements Serializable {

    private long timeStamp;
    private int databaseId, iconId, passwordStrength, category;
    private String applicationName, userName, icon, link, password;
    private boolean isFavourite, isCustomIcon;

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public int getIconId() {
        return iconId;
    }

    public int getPasswordStrength() {
        return passwordStrength;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getUserName() {
        return userName;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public boolean isCustomIcon() {
        return isCustomIcon;
    }

    public String getLink() {
        return link;
    }

    public int getCategory() {
        return category;
    }

    public String getPassword() {
        return password;
    }
}