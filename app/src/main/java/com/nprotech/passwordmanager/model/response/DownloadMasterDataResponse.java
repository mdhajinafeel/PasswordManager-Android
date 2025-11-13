package com.nprotech.passwordmanager.model.response;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class DownloadMasterDataResponse implements Serializable {

    private List<CategoriesResponse> categories;
    private List<IconsResponse> icons;

    public List<CategoriesResponse> getCategories() {
        return categories;
    }

    public List<IconsResponse> getIcons() {
        return icons;
    }
}