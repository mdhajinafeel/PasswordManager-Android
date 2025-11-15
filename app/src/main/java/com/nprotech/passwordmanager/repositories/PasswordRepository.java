package com.nprotech.passwordmanager.repositories;

import android.util.Base64;

import com.nprotech.passwordmanager.db.dao.PasswordDao;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.model.request.PasswordRequest;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PasswordRepository {

    private final PasswordDao passwordDao;

    @Inject
    public PasswordRepository(PasswordDao passwordDao) {
        this.passwordDao = passwordDao;
    }

    public List<PasswordModel> getPasswords() {
        return passwordDao.getPasswords();
    }

    public PasswordModel getPasswordModel(long timeStamp) {
        return passwordDao.getPasswordModel(timeStamp);
    }

    public PasswordEntity getPassword(long timeStamp) {
        return passwordDao.getPassword(timeStamp);
    }

    public long savePassword(PasswordEntity passwordEntity) {

        long savePassword = 0;

        try {

            PasswordRequest passwordRequest = new PasswordRequest();
            passwordRequest.setTimeStamp(passwordEntity.getTimeStamp());
            passwordRequest.setApplicationName(passwordEntity.getApplicationName());
            passwordRequest.setUserName(passwordEntity.getUserName());
            passwordRequest.setLink(passwordEntity.getLink());
            passwordRequest.setCategory(passwordEntity.getCategory());
            passwordRequest.setFavourite(passwordEntity.isFavourite());
            passwordRequest.setIconId(passwordEntity.getIconId());
            passwordRequest.setCustomIcon(passwordEntity.isCustomIcon());
            passwordRequest.setIcon(Base64.encodeToString(passwordEntity.getIcon(), Base64.NO_WRAP));

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