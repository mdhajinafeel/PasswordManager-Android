package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;

@SuppressWarnings("unused")
public class CategoriesResponse implements Serializable {

    private int id;
    private String categoryName, iconText;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getIconText() {
        return iconText;
    }
}