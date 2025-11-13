package com.nprotech.passwordmanager.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

@SuppressWarnings("unused")
public class SyncInterval implements Serializable {

    private final int id;
    private final String label;
    private final int hours;

    public SyncInterval(int id, String label, int hours) {
        this.id = id;
        this.label = label;
        this.hours = hours;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public int getHours() { return hours; }

    @NonNull
    @Override
    public String toString() {
        return label;
    }
}