package com.nprotech.passwordmanager.repositories;

import com.nprotech.passwordmanager.db.dao.PasswordDao;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.model.request.PasswordRequest;
import com.nprotech.passwordmanager.model.response.PasswordResponse;
import com.nprotech.passwordmanager.services.IPasswordApiService;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class PasswordRepository {

    private final IPasswordApiService passwordApiService;
    private final PasswordDao passwordDao;

    @Inject
    public PasswordRepository(IPasswordApiService passwordApiService, PasswordDao passwordDao) {
        this.passwordApiService = passwordApiService;
        this.passwordDao = passwordDao;
    }

    public List<PasswordModel> getPasswords() {
        return passwordDao.getPasswords();
    }

    public List<PasswordModel> getPasswordsFavorites() {
        return passwordDao.getPasswordsFavorites();
    }

    public PasswordModel getPasswordModel(long timeStamp) {
        return passwordDao.getPasswordModel(timeStamp);
    }

    public PasswordEntity getPassword(long timeStamp) {
        return passwordDao.getPassword(timeStamp);
    }

    public List<PasswordModel> getPasswordsByCategory(int categoryId) {
        return passwordDao.getPasswordsByCategory(categoryId);
    }

    public Call<PasswordResponse> savePassword(PasswordRequest passwordRequest) {
        return passwordApiService.savePassword(passwordRequest);
    }

    public long savePasswordDB(PasswordEntity passwordEntity) {

        long savePassword = 0;

        try {
            savePassword = passwordDao.insertPassword(passwordEntity);
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error savePassword", e);
        }

        return savePassword;
    }

    public int updatePassword(PasswordEntity passwordEntity) {

        int updatedPassword = 0;

        try {
            updatedPassword = passwordDao.updatedPassword(passwordEntity.getTimeStamp(), passwordEntity.getApplicationName(), passwordEntity.getUserName(), passwordEntity.getLink(),
                    passwordEntity.getCategory(), passwordEntity.getPassword(), passwordEntity.isCustomIcon(), passwordEntity.getIconId(), passwordEntity.getIcon());
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error updatePassword", e);
        }

        return updatedPassword;

    }

    public void updateFavourite(long timeStamp, boolean isFavourite) {
        passwordDao.updateFavourite(timeStamp, isFavourite);
    }
}