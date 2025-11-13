package com.nprotech.passwordmanager.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "categories")
public class CategoryEntity implements Serializable {

    @PrimaryKey
    private int id;
    private String categoryName;
    private String iconText;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIconText() {
        return iconText;
    }

    public void setIconText(String iconText) {
        this.iconText = iconText;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}