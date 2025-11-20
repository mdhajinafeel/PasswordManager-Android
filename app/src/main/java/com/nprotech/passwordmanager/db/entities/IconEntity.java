package com.nprotech.passwordmanager.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents an icon in the database.
 * This entity is used to store icon data as a byte array, associated with a name.
 */
@Entity(tableName = "icons")
public class IconEntity implements Serializable {

    /**
     * The unique identifier for the icon.
     */
    @PrimaryKey
    private int id;

    /**
     * The name of the icon.
     */
    private String name;

    /**
     * The icon data, stored as a byte array.
     */
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] icon;

    /**
     * Gets the unique identifier of the icon.
     *
     * @return The icon ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the icon.
     *
     * @param id The icon ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the icon.
     *
     * @return The icon name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the icon.
     *
     * @param name The icon name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the icon data.
     *
     * @return The icon data as a byte array.
     */
    public byte[] getIcon() {
        return icon;
    }

    /**
     * Sets the icon data.
     *
     * @param icon The icon data as a byte array.
     */
    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
}
