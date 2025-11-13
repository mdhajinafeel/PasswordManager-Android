package com.nprotech.passwordmanager.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "scheduler")
public class SchedulerEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int apiId;
    private String apiName;
    private long apiCalledAt;
    private boolean status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public long getApiCalledAt() {
        return apiCalledAt;
    }

    public void setApiCalledAt(long apiCalledAt) {
        this.apiCalledAt = apiCalledAt;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}