package com.nprotech.passwordmanager.repositories;

import com.nprotech.passwordmanager.db.dao.PasswordDao;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;

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
        return passwordDao.insertPassword(passwordEntity);
    }

    public long updatePassword(PasswordEntity passwordEntity) {
        return passwordDao.updatedPassword(passwordEntity.getTimeStamp(), passwordEntity.getApplicationName(), passwordEntity.getUserName(), passwordEntity.getLink(),
                passwordEntity.getCategory(), passwordEntity.getPassword(), passwordEntity.isCustomIcon(), passwordEntity.getIconId(), passwordEntity.getIcon());
    }

    public void updateFavourite(long timeStamp, boolean isFavourite) {
        passwordDao.updateFavourite(timeStamp, isFavourite);
    }
}