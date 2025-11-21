package com.nprotech.passwordmanager.viewmodel;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.db.entities.IconEntity;
import com.nprotech.passwordmanager.model.response.CategoriesResponse;
import com.nprotech.passwordmanager.model.response.DownloadMasterDataResponse;
import com.nprotech.passwordmanager.model.response.DownloadMasterResponse;
import com.nprotech.passwordmanager.model.response.IconsResponse;
import com.nprotech.passwordmanager.repositories.MasterRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class MasterViewModel extends ViewModel {

    private final Context context;
    private final MasterRepository masterRepository;
    private final MutableLiveData<Boolean> progressState = new MutableLiveData<>();
    private final MutableLiveData<String> errorTitle = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryEntity>> categoryEntityLiveData = new MutableLiveData<>();

    @Inject
    public MasterViewModel(@ApplicationContext Context context, MasterRepository masterRepository) {
        this.context = context;
        this.masterRepository = masterRepository;
    }

    public void masterDownload() {
        progressState.postValue(true);
        masterRepository.manualMasterDownload().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DownloadMasterResponse> call, @NonNull Response<DownloadMasterResponse> response) {
                progressState.postValue(false);
                if (response.isSuccessful() && response.body() != null) {

                    DownloadMasterDataResponse downloadMasterDataResponse = response.body().getData();
                    if (downloadMasterDataResponse != null) {

                        List<CategoriesResponse> categoriesResponse = downloadMasterDataResponse.getCategories();
                        if (categoriesResponse != null && !categoriesResponse.isEmpty()) {

                            masterRepository.deleteCategories();

                            List<CategoryEntity> categoryEntityList = new ArrayList<>();
                            for (CategoriesResponse category : categoriesResponse) {
                                CategoryEntity categoryEntity = new CategoryEntity();
                                categoryEntity.setId(category.getId());
                                categoryEntity.setCategoryName(category.getCategoryName());
                                categoryEntity.setIconText(category.getIconText());

                                categoryEntityList.add(categoryEntity);
                            }

                            if (!categoryEntityList.isEmpty()) {
                                masterRepository.insertCategories(categoryEntityList);
                            }
                        }

                        List<IconsResponse> iconsResponse = downloadMasterDataResponse.getIcons();
                        if (iconsResponse != null && !iconsResponse.isEmpty()) {
                            masterRepository.deleteIcons();

                            List<IconEntity> iconEntityList = new ArrayList<>();
                            for (IconsResponse icon : iconsResponse) {
                                IconEntity iconEntity = new IconEntity();
                                iconEntity.setId(icon.getId());
                                iconEntity.setName(icon.getIconName());
                                iconEntity.setIcon(Base64.decode(icon.getIcon(), Base64.DEFAULT));

                                iconEntityList.add(iconEntity);
                            }

                            if (!iconEntityList.isEmpty()) {
                                masterRepository.insertIcons(iconEntityList);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DownloadMasterResponse> call, @NonNull Throwable t) {
                progressState.postValue(false);
                errorTitle.setValue(context.getString(R.string.text_error));
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public LiveData<Boolean> getProgressState() {
        return progressState;
    }

    public LiveData<String> getErrorTitle() {
        return errorTitle;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public List<CategoryEntity> getAllCategories() {
        return masterRepository.getAllCategories();
    }

    public CategoryEntity getCategoryById(int id) {
        return masterRepository.getCategoryById(id);
    }

    public void getAllCategoriesLiveData() {
        categoryEntityLiveData.postValue(masterRepository.getAllCategories());
    }

    public LiveData<List<CategoryEntity>> getCategoryEntityLiveData() {
        return categoryEntityLiveData;
    }

    public List<IconEntity> getAllIcons() {
        return masterRepository.getAllIcons();
    }

    public void clearScheduler() {
        masterRepository.clearScheduler();
    }
}