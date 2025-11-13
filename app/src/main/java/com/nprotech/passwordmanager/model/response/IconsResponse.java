package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class IconsResponse implements Serializable {

    private int id;
    private String iconName, icon;

    public int getId() {
        return id;
    }

    public String getIconName() {
        return iconName;
    }

    public String getIcon() {
        return icon;
    }

}