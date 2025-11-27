package com.nprotech.passwordmanager.model.request;

import java.io.Serializable;

public class FavouriteRequest implements Serializable {

    private long timeStamp;
    private boolean isFavourite;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}