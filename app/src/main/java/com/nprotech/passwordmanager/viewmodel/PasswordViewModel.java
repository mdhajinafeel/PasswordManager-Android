package com.nprotech.passwordmanager.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.repositories.PasswordRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class PasswordViewModel extends ViewModel {

    private final PasswordRepository passwordRepository;
    private final Context context;
    private final MutableLiveData<Boolean> saveStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressState = new MutableLiveData<>();
    private String errorMessage, errorTitle;

    @Inject
    public PasswordViewModel(PasswordRepository passwordRepository, @ApplicationContext Context context) {
        this.passwordRepository = passwordRepository;
        this.context = context;
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<Boolean> getSaveStatus() {
        return saveStatus;
    }

    public LiveData<Boolean> getUpdateStatus() {
        return updateStatus;
    }

    public List<PasswordModel> getPasswords() {
        return passwordRepository.getPasswords();
    }

    public PasswordEntity getPassword(long timeStamp) {
        return passwordRepository.getPassword(timeStamp);
    }

    public PasswordModel getPasswordModel(long timeStamp) {
        return passwordRepository.getPasswordModel(timeStamp);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public void savePassword(PasswordEntity passwordEntity) {
        progressState.postValue(true);
        if(passwordRepository.savePassword(passwordEntity) > 0) {
            progressState.postValue(false);
            saveStatus.postValue(true);
        } else {
            progressState.postValue(false);
            saveStatus.postValue(false);
            setErrorTitle(context.getString(R.string.text_error));
            setErrorMessage(context.getString(R.string.password_save_failed));
        }
    }

    public void updatePassword(PasswordEntity passwordEntity) {
        progressState.postValue(true);
        if(passwordRepository.updatePassword(passwordEntity) > 0) {
            progressState.postValue(false);
            updateStatus.postValue(true);
        } else {
            progressState.postValue(false);
            updateStatus.postValue(false);
            setErrorTitle(context.getString(R.string.text_error));
            setErrorMessage(context.getString(R.string.password_save_failed));
        }
    }

    public void updateFavourite(long timeStamp, boolean isFavourite) {
        passwordRepository.updateFavourite(timeStamp, isFavourite);
    }
}