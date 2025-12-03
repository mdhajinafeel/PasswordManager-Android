package com.nprotech.passwordmanager.repositories;

import android.util.Base64;

import com.nprotech.passwordmanager.db.dao.CategoryDao;
import com.nprotech.passwordmanager.db.dao.IconDao;
import com.nprotech.passwordmanager.db.dao.PasswordDao;
import com.nprotech.passwordmanager.db.dao.SchedulerDao;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.db.entities.IconEntity;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.db.entities.SchedulerEntity;
import com.nprotech.passwordmanager.model.response.CategoriesResponse;
import com.nprotech.passwordmanager.model.response.DownloadMasterDataResponse;
import com.nprotech.passwordmanager.model.response.DownloadMasterResponse;
import com.nprotech.passwordmanager.model.response.IconsResponse;
import com.nprotech.passwordmanager.model.response.PasswordResponse;
import com.nprotech.passwordmanager.services.IMasterApiService;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final PasswordDao passwordDao;

    @Inject
    public MasterRepository(IMasterApiService masterApiService, CategoryDao categoryDao, IconDao iconDao, SchedulerDao schedulerDao,
                            PasswordDao passwordDao) {
        this.masterApiService = masterApiService;
        this.categoryDao = categoryDao;
        this.iconDao = iconDao;
        this.schedulerDao = schedulerDao;
        this.passwordDao = passwordDao;
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

                    List<PasswordResponse> passwordResponses = downloadMasterDataResponse.getPasswords();
                    if(passwordResponses != null && !passwordResponses.isEmpty()) {

                        List<PasswordEntity> passwordEntityList = mapPasswordsToEntities(passwordResponses);

                        if(!passwordEntityList.isEmpty()) {
                            deletePasswords();
                            insertPasswords(passwordEntityList);
                        }
                    }

                    schedulerDao.updatedScheduler(CommonUtils.masterApiId, true);
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error masterDownload", e);
        }
    }

    public List<CategoryEntity> mapCategoriesToEntities(List<CategoriesResponse> categoriesResponse) {
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

    public List<PasswordEntity> mapPasswordsToEntities(List<PasswordResponse> passwordResponses) {
        List<PasswordEntity> passwordEntityList = new ArrayList<>();
        for (PasswordResponse passwordResponse : passwordResponses) {
            PasswordEntity passwordEntity = new PasswordEntity();
            passwordEntity.setTimeStamp(passwordResponse.getTimeStamp());
            passwordEntity.setDatabaseId(passwordResponse.getDatabaseId());
            passwordEntity.setApplicationName(passwordResponse.getApplicationName());
            passwordEntity.setUserName(passwordResponse.getUserName());
            passwordEntity.setLink(passwordResponse.getLink());
            passwordEntity.setCategory(passwordResponse.getCategory());
            passwordEntity.setPassword(passwordResponse.getPassword());
            passwordEntity.setSynced(true);
            passwordEntity.setFavourite(passwordResponse.isFavourite());
            passwordEntity.setDeleted(false);
            passwordEntity.setCustomIcon(passwordResponse.isCustomIcon());

            if(passwordResponse.getIcon() != null && !Objects.equals(passwordResponse.getIcon(), "")) {
                byte[] decodedBytes = decodeBase64ToBytes(passwordResponse.getIcon());
                if (decodedBytes != null) {
                    passwordEntity.setIcon(decodedBytes);
                }
            } else {
                passwordEntity.setIcon(null);
            }

            passwordEntity.setIconId(passwordResponse.getIconId());
            passwordEntity.setPasswordStrength(passwordResponse.getPasswordStrength());

            passwordEntityList.add(passwordEntity);
        }

        return passwordEntityList;
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

    //PASSWORDS
    public void deletePasswords() {
        passwordDao.clearAll();
    }

    public void insertPasswords(List<PasswordEntity> passwordEntityList) {
         passwordDao.insertPasswords(passwordEntityList);
    }

    private byte[] decodeBase64ToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        // Remove "data:image/jpeg;base64," or similar prefixes if present
        if (base64String.contains(",")) {
            base64String = base64String.substring(base64String.indexOf(",") + 1);
        }
        return Base64.decode(base64String, Base64.DEFAULT);
    }
}