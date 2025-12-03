package com.nprotech.passwordmanager.viewmodel;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.model.request.FavouriteRequest;
import com.nprotech.passwordmanager.model.request.PasswordRequest;
import com.nprotech.passwordmanager.model.response.SavePasswordResponse;
import com.nprotech.passwordmanager.repositories.PasswordRepository;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for managing password-related data and interactions.
 * This class provides methods to save, update, and retrieve passwords,
 * while exposing LiveData for observing changes in the UI.
 */
@HiltViewModel
public class PasswordViewModel extends ViewModel {

    private final PasswordRepository passwordRepository;
    private final Context context;
    private final MutableLiveData<Boolean> saveStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressState = new MutableLiveData<>();
    private String errorMessage, errorTitle;

    /**
     * Constructs a new PasswordViewModel with the given repository and context.
     *
     * @param passwordRepository The repository for password data operations.
     * @param context            The application context.
     */
    @Inject
    public PasswordViewModel(PasswordRepository passwordRepository, @ApplicationContext Context context) {
        this.passwordRepository = passwordRepository;
        this.context = context;
    }

    /**
     * Gets the LiveData for observing the progress state of an operation.
     *
     * @return A LiveData object that emits true during an operation and false when it's complete.
     */
    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    /**
     * Gets the LiveData for observing the status of a save operation.
     *
     * @return A LiveData object that emits true on success and false on failure.
     */
    public LiveData<Boolean> getSaveStatus() {
        return saveStatus;
    }

    /**
     * Gets the LiveData for observing the status of an update operation.
     *
     * @return A LiveData object that emits true on success and false on failure.
     */
    public LiveData<Boolean> getUpdateStatus() {
        return updateStatus;
    }

    /**
     * Gets a list of all passwords.
     *
     * @return A list of {@link PasswordModel}.
     */
    public List<PasswordModel> getPasswords() {
        return passwordRepository.getPasswords();
    }

    public LiveData<List<PasswordModel>> getPasswordModelLiveData() {
        return passwordRepository.getPasswordsLive();
    }

    /**
     * Gets a password by its timestamp.
     *
     * @param timeStamp The timestamp of the password to retrieve.
     * @return The {@link PasswordEntity} corresponding to the timestamp.
     */
    public PasswordEntity getPassword(long timeStamp) {
        return passwordRepository.getPassword(timeStamp);
    }

    /**
     * Gets a list of all favourite passwords.
     *
     * @return A list of {@link PasswordModel}.
     */
    public List<PasswordModel> getPasswordsFavorites() {
        return passwordRepository.getPasswordsFavorites();
    }

    public LiveData<List<PasswordModel>> getPasswordsFavoritesLive() {
        return passwordRepository.getPasswordsFavoritesLive();
    }

    /**
     * Gets a password by its categoryId.
     *
     * @param categoryId The categoryId of the password to retrieve.
     * @return The {@link PasswordEntity} corresponding to the categoryId.
     */
    public List<PasswordModel> getPasswordsByCategory(int categoryId) {
        return passwordRepository.getPasswordsByCategory(categoryId);
    }

    /**
     * Gets a password model by its timestamp.
     *
     * @param timeStamp The timestamp of the password to retrieve.
     * @return The {@link PasswordModel} corresponding to the timestamp.
     */
    public PasswordModel getPasswordModel(long timeStamp) {
        return passwordRepository.getPasswordModel(timeStamp);
    }

    /**
     * Gets the current error message.
     *
     * @return The error message string.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage The error message to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the current error title.
     *
     * @return The error title string.
     */
    public String getErrorTitle() {
        return errorTitle;
    }

    /**
     * Sets the error title.
     *
     * @param errorTitle The error title to set.
     */
    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    /**
     * Saves a new password.
     * It updates the progress state and save status based on the result.
     *
     * @param passwordEntity The password to save.
     */
    public void savePassword(PasswordEntity passwordEntity) {
        try {
            if (passwordEntity.getTimeStamp() > 0) {

                progressState.postValue(true);

                PasswordRequest passwordRequest = new PasswordRequest();
                passwordRequest.setTimeStamp(passwordEntity.getTimeStamp());
                passwordRequest.setApplicationName(passwordEntity.getApplicationName());
                passwordRequest.setUserName(passwordEntity.getUserName());
                passwordRequest.setLink(passwordEntity.getLink());
                passwordRequest.setCategory(passwordEntity.getCategory());
                passwordRequest.setPassword(passwordEntity.getPassword());
                passwordRequest.setPasswordStrength(passwordEntity.getPasswordStrength());
                passwordRequest.setFavourite(passwordEntity.isFavourite());
                passwordRequest.setIconId(passwordEntity.getIconId());
                passwordRequest.setCustomIcon(passwordEntity.isCustomIcon());
                if (passwordEntity.getIcon() != null && passwordEntity.getIcon().length > 0) {
                    passwordRequest.setIcon(Base64.encodeToString(passwordEntity.getIcon(), Base64.NO_WRAP));
                } else {
                    passwordRequest.setIcon(null);
                }

                Call<SavePasswordResponse> passwordResponseCall = passwordRepository.savePassword(passwordRequest);
                passwordResponseCall.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<SavePasswordResponse> call, @NonNull Response<SavePasswordResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            SavePasswordResponse savePasswordResponse = response.body();

                            if (savePasswordResponse.isStatus()) {

                                passwordEntity.setDatabaseId(savePasswordResponse.getPasswordId());
                                passwordEntity.setSynced(true);

                                if (passwordRepository.savePasswordDB(passwordEntity) > 0) {
                                    saveStatus.postValue(true);
                                } else {
                                    saveStatus.postValue(false);
                                    setErrorTitle(context.getString(R.string.text_error));
                                    setErrorMessage(context.getString(R.string.password_save_failed));
                                }
                            } else {
                                progressState.postValue(false);
                                setErrorTitle(context.getString(R.string.text_error));
                                setErrorMessage(savePasswordResponse.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SavePasswordResponse> call, @NonNull Throwable t) {
                        progressState.postValue(false);
                        setErrorTitle(context.getString(R.string.text_error));
                        setErrorMessage(context.getString(R.string.common_error));
                        AppLogger.e(getClass(), "Error in savePassword", t);
                    }
                });
            }
        } catch (Exception e) {
            progressState.postValue(false);
            AppLogger.e(getClass(), "Error in savePassword", e);
        }
    }

    /**
     * Updates an existing password.
     * It updates the progress state and update status based on the result.
     *
     * @param passwordEntity The password to update.
     */
    public void updatePassword(PasswordEntity passwordEntity) {
        progressState.postValue(true);
        if (passwordRepository.updatePassword(passwordEntity) > 0) {
            progressState.postValue(false);
            updateStatus.postValue(true);
        } else {
            progressState.postValue(false);
            updateStatus.postValue(false);
            setErrorTitle(context.getString(R.string.text_error));
            setErrorMessage(context.getString(R.string.password_save_failed));
        }
    }

    /**
     * Saves a favourite password.
     * It updates the progress state and save status based on the result.
     *
     * @param favouriteRequest The password to favourite.
     */
    public void favouritePassword(FavouriteRequest favouriteRequest) {
        try {
            progressState.postValue(true);
            Call<SavePasswordResponse> favouriteResponseCall = passwordRepository.updateFavourite(favouriteRequest);
            favouriteResponseCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<SavePasswordResponse> call, @NonNull Response<SavePasswordResponse> response) {
                    progressState.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        SavePasswordResponse savePasswordResponse = response.body();
                        updateFavouriteDB(savePasswordResponse.getTimeStamp(), favouriteRequest.isFavourite());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SavePasswordResponse> call, @NonNull Throwable t) {
                    progressState.postValue(false);
                    updateFavouriteDB(favouriteRequest.getTimeStamp(), favouriteRequest.isFavourite());
                    AppLogger.e(getClass(), "Error in favouritePassword", t);
                }
            });
        } catch (Exception e) {
            progressState.postValue(false);
            AppLogger.e(getClass(), "Error in favouritePassword", e);
        }
    }

    /**
     * Updates the favourite status of a password.
     *
     * @param timeStamp   The timestamp of the password to update.
     * @param isFavourite True to mark as favourite, false otherwise.
     */
    public void updateFavouriteDB(long timeStamp, boolean isFavourite) {
        passwordRepository.updateFavouriteDB(timeStamp, isFavourite);
    }
}