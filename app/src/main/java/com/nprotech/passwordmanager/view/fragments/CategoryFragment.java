package com.nprotech.passwordmanager.view.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.view.adapter.CommonRecyclerViewAdapter;
import com.nprotech.passwordmanager.view.adapter.ViewHolder;
import com.nprotech.passwordmanager.viewmodel.MasterViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryFragment extends Fragment {

    private RecyclerView rvCategories;
    private AppCompatEditText etSearch;
    private FrameLayout frameNoData;
    private MasterViewModel masterViewModel;
    private CommonRecyclerViewAdapter<CategoryEntity> categoryEntityCommonRecyclerViewAdapter;

    public static CategoryFragment getInstance() {
        return new CategoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        try {
            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            frameNoData = view.findViewById(R.id.frameNoData);
            rvCategories = view.findViewById(R.id.rvCategories);
            etSearch = view.findViewById(R.id.etSearch);

            txtTitle.setText(getString(R.string.categories));

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            fetchCategories();
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error onCreateView", e);
        }

        return view;
    }

    private void fetchCategories() {
        try {
            List<CategoryEntity> categories = masterViewModel.getAllCategories();
            if (!categories.isEmpty()) {

                categoryEntityCommonRecyclerViewAdapter = new CommonRecyclerViewAdapter<>(
                        requireContext(), categories, R.layout.row_item_category) {
                    @Override
                    public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull CategoryEntity item) {

                        holder.setViewText(R.id.tvCategoryName, item.getCategoryName());
                        holder.setViewText(R.id.tvCategoryPassword, "0 Password");

                        CardView layoutForeground = holder.getView(R.id.layoutForeground);
                        layoutForeground.setCardBackgroundColor(Color.parseColor(item.getColorCode()));

                        String base64Icon = item.getIconText();
                        if (base64Icon != null && !base64Icon.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(base64Icon, Base64.DEFAULT);
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 1; // scale down if needed
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
                                holder.setViewImageBitmap(R.id.ivCategory, bitmap);
                            } catch (Exception e) {
                                AppLogger.e(getClass(), "createView", e);
                                // fallback image if decoding fails
                                holder.setViewImageDrawable(R.id.ivCategory, ContextCompat.getDrawable(requireContext(), R.drawable.ic_default_category));
                            }
                        } else {
                            holder.setViewImageDrawable(R.id.ivCategory, ContextCompat.getDrawable(requireContext(), R.drawable.ic_default_category));
                        }
                    }
                };

                rvCategories.setAdapter(categoryEntityCommonRecyclerViewAdapter);
                rvCategories.setVisibility(View.VISIBLE);
                etSearch.setVisibility(View.VISIBLE);
                frameNoData.setVisibility(View.GONE);

                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterList(s.toString());
                    }
                });
            } else {
                etSearch.setVisibility(View.GONE);
                rvCategories.setVisibility(View.GONE);
                frameNoData.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error fetchCategories", e);
        }
    }

    private void filterList(String text) {
        List<CategoryEntity> filteredList = new ArrayList<>();
        for (CategoryEntity item : masterViewModel.getAllCategories()) {
            if (item.getCategoryName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        categoryEntityCommonRecyclerViewAdapter.updateData(filteredList);
    }
}