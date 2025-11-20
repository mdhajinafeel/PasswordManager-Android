package com.nprotech.passwordmanager.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents a category in the database.
 * This entity is used to store information about different categories of passwords.
 */
@Entity(tableName = "categories")
public class CategoryEntity implements Serializable {

    /**
     * The unique identifier for the category.
     */
    @PrimaryKey
    private int id;
    /**
     * The name of the category.
     */
    private String categoryName;
    /**
     * The text representation of the icon for the category.
     */
    private String iconText;
    /**
     * The color code of the category.
     */
    private String colorCode;

    /**
     * Gets the unique identifier of the category.
     *
     * @return The category ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the category.
     *
     * @param id The category ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the icon text for the category.
     *
     * @return The icon text.
     */
    public String getIconText() {
        return iconText;
    }

    /**
     * Sets the icon text for the category.
     *
     * @param iconText The icon text to set.
     */
    public void setIconText(String iconText) {
        this.iconText = iconText;
    }

    /**
     * Gets the name of the category.
     *
     * @return The category name.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Sets the name of the category.
     *
     * @param categoryName The category name to set.
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Gets the color code of the category.
     *
     * @return The color code.
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * Sets the color code of the category.
     *
     * @param colorCode The category color code to set.
     */
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
}