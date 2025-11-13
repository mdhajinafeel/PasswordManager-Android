package com.nprotech.passwordmanager.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.model.request.LoginRequest;
import com.nprotech.passwordmanager.model.request.SignUpRequest;
import com.nprotech.passwordmanager.model.response.LoginResponse;
import com.nprotech.passwordmanager.model.response.LogoutResponse;
import com.nprotech.passwordmanager.repositories.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final Context context;
    private final MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logout = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private String errorTitle, errorMessage;
    private final MutableLiveData<Boolean> progressState = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthRepository authRepository, @ApplicationContext Context context) {
        this.authRepository = authRepository;
        this.context = context;
    }

    public LiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public void register(SignUpRequest signUpRequest) {
        progressState.postValue(true);
        authRepository.authRegister(signUpRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                progressState.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    loginStatus.postValue(true);
                    loginResult.setValue(response.body());
                } else {
                    loginStatus.postValue(false);
                    setErrorTitle(context.getString(R.string.register_failed));
                    setErrorMessage(response.body() != null ? response.body().getMessage() : context.getString(R.string.common_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressState.postValue(false);
                setErrorTitle(context.getString(R.string.text_error));
                setErrorMessage(t.getMessage());
                loginStatus.postValue(false);
            }
        });
    }

    public void login(LoginRequest loginRequest) {
        progressState.postValue(true);
        authRepository.authLogin(loginRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                progressState.postValue(false);
                if (response.isSuccessful() && response.body() != null) {

                    if(!response.body().isStatus()) {
                        loginStatus.postValue(false);
                        setErrorTitle(context.getString(R.string.login_failed));
                        setErrorMessage(response.body().getMessage() != null ? response.body().getMessage() : context.getString(R.string.common_error));
                    } else {
                        loginStatus.postValue(true);
                        loginResult.setValue(response.body());
                    }
                } else {
                    loginStatus.postValue(false);
                    setErrorTitle(context.getString(R.string.login_failed));
                    setErrorMessage(response.body() != null ? response.body().getMessage() : context.getString(R.string.common_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressState.postValue(false);
                loginStatus.postValue(false);
                setErrorTitle(context.getString(R.string.text_error));
                setErrorMessage(t.getMessage());
            }
        });
    }

    public void logout() {
        progressState.postValue(true);
        authRepository.authLogout().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LogoutResponse> call, @NonNull Response<LogoutResponse> response) {
                progressState.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if(response.body().isStatus()) {
                        logout.postValue(true);
                    }
                } else {
                    logout.postValue(false);
                    setErrorTitle(context.getString(R.string.logout_failed));
                    setErrorMessage(response.body() != null ? response.body().getMessage() : context.getString(R.string.common_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LogoutResponse> call, @NonNull Throwable t) {
                progressState.postValue(false);
                logout.postValue(false);
                setErrorTitle(context.getString(R.string.text_error));
                setErrorMessage(t.getMessage());
            }
        });
    }

    public LiveData<Boolean> getLogout() {
        return logout;
    }

    public LiveData<Boolean> getLoginStatus() {
        return loginStatus;
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
}