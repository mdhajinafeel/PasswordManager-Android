package com.nprotech.passwordmanager.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents a scheduler entry in the database.
 * This entity is used to track the status and timing of API calls.
 */
@Entity(tableName = "scheduler")
public class SchedulerEntity implements Serializable {

    /**
     * The unique identifier for the scheduler entry.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;
    /**
     * The ID of the API that was called.
     */
    private int apiId;
    /**
     * The name of the API that was called.
     */
    private String apiName;
    /**
     * The timestamp of when the API was called.
     */
    private long apiCalledAt;
    /**
     * The status of the API call (e.g., true for success, false for failure).
     */
    private boolean status;

    /**
     * Gets the unique identifier of the scheduler entry.
     *
     * @return The scheduler entry ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the scheduler entry.
     *
     * @param id The scheduler entry ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the ID of the API.
     *
     * @return The API ID.
     */
    public int getApiId() {
        return apiId;
    }

    /**
     * Sets the ID of the API.
     *
     * @param apiId The API ID to set.
     */
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    /**
     * Gets the name of the API.
     *
     * @return The API name.
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * Sets the name of the API.
     *
     * @param apiName The API name to set.
     */
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    /**
     * Gets the timestamp of when the API was called.
     *
     * @return The timestamp of the API call.
     */
    public long getApiCalledAt() {
        return apiCalledAt;
    }

    /**
     * Sets the timestamp of when the API was called.
     *
     * @param apiCalledAt The timestamp of the API call.
     */
    public void setApiCalledAt(long apiCalledAt) {
        this.apiCalledAt = apiCalledAt;
    }

    /**
     * Gets the status of the API call.
     *
     * @return True if the call was successful, false otherwise.
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * Sets the status of the API call.
     *
     * @param status The status to set.
     */
    public void setStatus(boolean status) {
        this.status = status;
    }
}
