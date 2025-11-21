package com.nprotech.passwordmanager.repositories;

import android.util.Base64;

import androidx.lifecycle.LiveData;

import com.nprotech.passwordmanager.db.dao.CategoryDao;
import com.nprotech.passwordmanager.db.dao.IconDao;
import com.nprotech.passwordmanager.db.dao.SchedulerDao;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.db.entities.IconEntity;
import com.nprotech.passwordmanager.db.entities.SchedulerEntity;
import com.nprotech.passwordmanager.model.response.CategoriesResponse;
import com.nprotech.passwordmanager.model.response.DownloadMasterDataResponse;
import com.nprotech.passwordmanager.model.response.DownloadMasterResponse;
import com.nprotech.passwordmanager.model.response.IconsResponse;
import com.nprotech.passwordmanager.services.IMasterApiService;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class MasterRepository {

    private final IMasterApiService masterApiService;
    private final CategoryDao categoryDao;
    private final IconDao iconDao;
    private final SchedulerDao schedulerDao;

    @Inject
    public MasterRepository(IMasterApiService masterApiService, CategoryDao categoryDao, IconDao iconDao, SchedulerDao schedulerDao) {
        this.masterApiService = masterApiService;
        this.categoryDao = categoryDao;
        this.iconDao = iconDao;
        this.schedulerDao = schedulerDao;
    }

    public void masterDownload() {
        try {

            SchedulerEntity schedulerEntity = new SchedulerEntity();
            schedulerEntity.setApiId(CommonUtils.masterApiId);
            schedulerEntity.setApiName(CommonUtils.masterApiName);
            schedulerEntity.setApiCalledAt(CommonUtils.getCurrentDateTimeStamp(false));
            schedulerEntity.setStatus(false);
            schedulerDao.insertScheduler(schedulerEntity);

            Response<DownloadMasterResponse> response = masterApiService.masterDownload().execute();
            if (response.isSuccessful() && response.body() != null) {
                DownloadMasterDataResponse downloadMasterDataResponse = response.body().getData();

                if (downloadMasterDataResponse != null) {

                    List<CategoriesResponse> categoriesResponse = downloadMasterDataResponse.getCategories();

                    if (categoriesResponse != null && !categoriesResponse.isEmpty()) {

                        List<CategoryEntity> categoryEntityList = mapCategoriesToEntities(categoriesResponse);

                        if (!categoryEntityList.isEmpty()) {
                            deleteCategories();
                            insertCategories(categoryEntityList);
                        }
                    }

                    List<IconsResponse> iconsResponse = downloadMasterDataResponse.getIcons();
                    if (iconsResponse != null && !iconsResponse.isEmpty()) {
                        deleteIcons();

                        List<IconEntity> iconEntityList = new ArrayList<>();
                        for (IconsResponse icon : iconsResponse) {
                            IconEntity iconEntity = new IconEntity();
                            iconEntity.setId(icon.getId());
                            iconEntity.setName(icon.getIconName());
                            iconEntity.setIcon(Base64.decode(icon.getIcon(), Base64.DEFAULT));

                            iconEntityList.add(iconEntity);
                        }

                        if (!iconEntityList.isEmpty()) {
                            insertIcons(iconEntityList);
                        }
                    }

                    schedulerDao.updatedScheduler(CommonUtils.masterApiId, true);
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error masterDownload", e);
        }
    }

    private List<CategoryEntity> mapCategoriesToEntities(List<CategoriesResponse> categoriesResponse) {
        List<CategoryEntity> categoryEntityList = new ArrayList<>();

        if (categoriesResponse == null || categoriesResponse.isEmpty()) {
            return categoryEntityList;
        }

        for (CategoriesResponse category : categoriesResponse) {
            CategoryEntity entity = new CategoryEntity();
            entity.setId(category.getId());
            entity.setCategoryName(category.getCategoryName());
            entity.setIconText(category.getIconText());
            entity.setColorCode(category.getColorCode());

            categoryEntityList.add(entity);
        }

        return categoryEntityList;
    }

    public Call<DownloadMasterResponse> manualMasterDownload() {
        return masterApiService.masterDownload();
    }

    //CATEGORIES
    public void insertCategories(List<CategoryEntity> categoryEntityList) {
        categoryDao.insertCategories(categoryEntityList);
    }

    public void deleteCategories() {
        categoryDao.clearAll();
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public CategoryEntity getCategoryById(int id) {
        return categoryDao.getCategoryById(id);
    }

    //ICONS
    public void insertIcons(List<IconEntity> iconEntityList) {
        iconDao.insertIcons(iconEntityList);
    }

    public void deleteIcons() {
        iconDao.clearAll();
    }

    public List<IconEntity> getAllIcons() {
        return iconDao.getAllIcons();
    }

    public void clearScheduler() {
        schedulerDao.clearAll();
    }
}