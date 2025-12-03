package com.nprotech.passwordmanager.view.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.helper.CryptoHelper;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.model.PasswordStrength;
import com.nprotech.passwordmanager.model.request.FavouriteRequest;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.view.activities.AddPasswordActivity;
import com.nprotech.passwordmanager.view.activities.MainActivity;
import com.nprotech.passwordmanager.view.adapter.CommonRecyclerViewAdapter;
import com.nprotech.passwordmanager.view.adapter.ViewHolder;
import com.nprotech.passwordmanager.viewmodel.PasswordViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment {

    private RecyclerView rvPasswordList, rvPasswordStrengthType;
    private AppCompatEditText etSearch;
    private AppCompatTextView tvNoDataFound;
    private PasswordViewModel passwordViewModel;
    private FrameLayout frameNoData;
    private CommonRecyclerViewAdapter<PasswordModel> passwordEntityCommonRecyclerViewAdapter;
    private RecyclerView.ViewHolder currentSwipedHolder;
    private int selectedStrength = -1;   // -1 = nothing selected

    public static FavoritesFragment getInstance() {
        return new FavoritesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        try {
            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            tvNoDataFound = view.findViewById(R.id.tvNoDataFound);
            rvPasswordStrengthType = view.findViewById(R.id.rvPasswordStrengthType);
            rvPasswordList = view.findViewById(R.id.rvPasswordList);
            etSearch = view.findViewById(R.id.etSearch);
            frameNoData = view.findViewById(R.id.frameNoData);

            passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

            txtTitle.setText(getString(R.string.favourites));

            passwordViewModel.getProgressState().observe(getViewLifecycleOwner(), isProgress -> {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    if (isProgress) {
                        mainActivity.showProgress();  // call showProgress
                    } else {
                        mainActivity.hideProgress();  // call hideProgress
                    }
                }
            });

            fetchPasswords();
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error onCreateView", e);
        }

        return view;
    }

    private void fetchPasswords() {
        try {
            passwordViewModel.getPasswordsFavoritesLive().observe(getViewLifecycleOwner(), passwordModels -> {
                List<PasswordModel> passwordList = new ArrayList<>(passwordModels);
                if (!passwordList.isEmpty()) {

                    fetchPasswordStrengthType();

                    passwordEntityCommonRecyclerViewAdapter = new CommonRecyclerViewAdapter<>(requireContext(), passwordList,
                            R.layout.row_item_passwords) {
                        @Override
                        public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull PasswordModel item) {
                            try {

                                holder.setViewText(R.id.tvApplicationName, item.getApplicationName());
                                holder.setViewText(R.id.tvUsername, item.getUserName());

                                AppCompatTextView tvPassword = holder.getView(R.id.tvPassword);
                                tvPassword.setText("••••••••");

                                String decryptedPassword = CryptoHelper.decrypt(item.getPassword());

                                // Password Toggle
                                AppCompatCheckBox cbTogglePassword = holder.getView(R.id.cbTogglePassword);
                                cbTogglePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) {
                                        tvPassword.setText(decryptedPassword);
                                    } else {
                                        tvPassword.setText("••••••••");
                                    }
                                });

                                // Icon
                                if (item.getIcon() != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(item.getIcon(), 0, item.getIcon().length);
                                    holder.setViewImageBitmap(R.id.imgPasswordIcon, bitmap);
                                }

                                // Favourite
                                AppCompatCheckBox cbFavorite = holder.getView(R.id.cbFavorite);

                                // 1. Remove previous listener
                                cbFavorite.setOnCheckedChangeListener(null);

                                // 2. Set checked state WITHOUT triggering listener
                                cbFavorite.setChecked(item.isFavourite());

                                // 3. Re-attach listener AFTER setting checked
                                cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {

                                    FavouriteRequest favouriteRequest = new FavouriteRequest();
                                    favouriteRequest.setTimeStamp(item.getTimeStamp());
                                    favouriteRequest.setFavourite(isChecked);
                                    passwordViewModel.favouritePassword(favouriteRequest);

                                    List<PasswordModel> newList = passwordViewModel.getPasswordsFavorites();

                                    rvPasswordList.post(() -> {
                                        passwordEntityCommonRecyclerViewAdapter.updateData(newList);

                                        if (newList.isEmpty()) {
                                            rvPasswordList.setVisibility(View.GONE);
                                            frameNoData.setVisibility(View.VISIBLE);
                                        } else {
                                            rvPasswordList.setVisibility(View.VISIBLE);
                                            frameNoData.setVisibility(View.GONE);
                                        }
                                    });
                                });

                                // Copy Password
                                holder.getView(R.id.ivCopyPassword).setOnClickListener(v -> {
                                    ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("password", decryptedPassword);
                                    clipboard.setPrimaryClip(clip);

                                    Toast.makeText(requireContext(), "Password copied", Toast.LENGTH_SHORT).show();
                                });

                                // Password Strength
                                AppCompatTextView tvStrengthLabel = holder.getView(R.id.tvStrengthLabel);
                                View viewStrengthIndicator = holder.getView(R.id.viewStrengthIndicator);
                                Drawable background = viewStrengthIndicator.getBackground();

                                int colorRes;
                                int textRes;

                                switch (item.getPasswordStrength()) {
                                    case 2:
                                        colorRes = R.color.mediumColor;
                                        textRes = R.string.medium;
                                        break;

                                    case 3:
                                        colorRes = R.color.strongColor;
                                        textRes = R.string.strong;
                                        break;

                                    case 4:
                                        colorRes = R.color.veryStrongColor;
                                        textRes = R.string.very_strong;
                                        break;

                                    default:
                                        colorRes = R.color.weakColor;
                                        textRes = R.string.weak;
                                        break;
                                }

                                // Apply color to indicator
                                if (background instanceof GradientDrawable) {
                                    ((GradientDrawable) background).setColor(
                                            ContextCompat.getColor(requireContext(), colorRes)
                                    );
                                }

                                // Set text + text color
                                tvStrengthLabel.setText(getString(textRes));
                                tvStrengthLabel.setTextColor(ContextCompat.getColor(requireContext(), colorRes));

                                // Swipe
                                holder.getView(R.id.ivEdit).setOnClickListener(v -> {
                                    closeSwipe(holder);
                                    editPassword(item);
                                });

                                holder.getView(R.id.ivDelete).setOnClickListener(v -> {
                                    closeSwipe(holder);
                                    Toast.makeText(v.getContext(), "Deleted " + item.getTimeStamp(), Toast.LENGTH_SHORT).show();
                                });
                            } catch (Exception e) {
                                AppLogger.e(getInstance().getClass(), "Error Password List", e);
                            }

                        }
                    };

                    rvPasswordList.setAdapter(passwordEntityCommonRecyclerViewAdapter);
                    rvPasswordList.setVisibility(View.VISIBLE);
                    etSearch.setVisibility(View.VISIBLE);
                    frameNoData.setVisibility(View.GONE);
                    setupSwipeToReveal();

                    etSearch.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            applyCombinedFilter();
                        }
                    });
                } else {
                    etSearch.setVisibility(View.GONE);
                    rvPasswordList.setVisibility(View.GONE);
                    rvPasswordStrengthType.setVisibility(View.GONE);
                    frameNoData.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error fetchPasswords", e);
        }
    }

    private void fetchPasswordStrengthType() {
        try {

            List<PasswordStrength> passwordStrengthList = new ArrayList<>();
            passwordStrengthList.add(new PasswordStrength(
                    CommonUtils.passwordWeak,
                    ContextCompat.getColor(requireContext(), R.color.weakColor),
                    getString(R.string.weak),
                    R.drawable.bg_rectangle_weak_border,
                    R.drawable.bg_rectangle_weak_border_dark
            ));

            passwordStrengthList.add(new PasswordStrength(
                    CommonUtils.passwordMedium,
                    ContextCompat.getColor(requireContext(), R.color.mediumColor),
                    getString(R.string.medium),
                    R.drawable.bg_rectangle_medium_border,
                    R.drawable.bg_rectangle_medium_border_dark
            ));

            passwordStrengthList.add(new PasswordStrength(
                    CommonUtils.passwordStrong,
                    ContextCompat.getColor(requireContext(), R.color.strongColor),
                    getString(R.string.strong),
                    R.drawable.bg_rectangle_strong_border,
                    R.drawable.bg_rectangle_strong_border_dark
            ));

            passwordStrengthList.add(new PasswordStrength(
                    CommonUtils.passwordVeryStrong,
                    ContextCompat.getColor(requireContext(), R.color.veryStrongColor),
                    getString(R.string.very_strong),
                    R.drawable.bg_rectangle_verystrong_border,
                    R.drawable.bg_rectangle_verystrong_border_dark
            ));

            CommonRecyclerViewAdapter<PasswordStrength> passwordStrengthCommonRecyclerViewAdapter = new CommonRecyclerViewAdapter<>(requireContext(),
                    passwordStrengthList, R.layout.row_item_passwordstrength) {

                // ⬇️ ⬇️ ADD HELPER METHODS HERE ⬇️ ⬇️
                private void setBackground(View view, @DrawableRes int resId) {
                    view.setBackgroundResource(resId);
                    view.setTag(R.id.tag_bg_res_id, resId);
                }
                // ⬆️ ⬆️ HELPER METHODS END ⬆️ ⬆️

                @Override
                public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull PasswordStrength item) {

                    // your binding code here
                    RelativeLayout rlPasswordStrength = holder.getView(R.id.rlPasswordStrength);

                    int normalBg = item.getBackgroundResNormal();
                    int darkBg = item.getBackgroundResDark();

                    // set initial background using helper
                    if (item.getStrengthType() == selectedStrength) {
                        setBackground(rlPasswordStrength, darkBg);
                    } else {
                        setBackground(rlPasswordStrength, normalBg);
                    }

                    holder.setViewText(R.id.tvStrengthLabel, item.getStrengthLabel());
                    holder.setViewTextColor(R.id.tvStrengthLabel, item.getColor());

                    View viewStrengthIndicator = holder.getView(R.id.viewStrengthIndicator);
                    Drawable drawable = viewStrengthIndicator.getBackground();
                    if (drawable instanceof GradientDrawable) {
                        GradientDrawable gd = (GradientDrawable) drawable.mutate();
                        gd.setColor(item.getColor());
                        viewStrengthIndicator.setBackground(gd);
                    }

                    rlPasswordStrength.setOnClickListener(v -> {

                        boolean isSelected = (item.getStrengthType() == selectedStrength);

                        if (isSelected) {
                            selectedStrength = -1;
                            setBackground(rlPasswordStrength, normalBg);
                            notifyDataSetChanged();
                            applyCombinedFilter();
                            return;
                        }

                        selectedStrength = item.getStrengthType();
                        setBackground(rlPasswordStrength, darkBg);
                        notifyDataSetChanged();
                        applyCombinedFilter();
                    });
                }
            };

            rvPasswordStrengthType.setAdapter(passwordStrengthCommonRecyclerViewAdapter);
            rvPasswordStrengthType.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error fetchPasswordStrengthType", e);
        }
    }

    private void setupSwipeToReveal() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Prevent auto delete
                passwordEntityCommonRecyclerViewAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 1.0f; // never auto-delete
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View foreground = viewHolder.itemView.findViewById(R.id.layoutForeground);

                float maxSwipe = -250f; // how far left it can go
                float translationX = Math.max(dX, maxSwipe);

                if (isCurrentlyActive) {
                    foreground.setTranslationX(translationX);
                } else {
                    if (Math.abs(foreground.getTranslationX()) > Math.abs(maxSwipe) / 3) {
                        openSwipe(viewHolder);
                    } else {
                        closeSwipe(viewHolder);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvPasswordList);
    }

    private void openSwipe(RecyclerView.ViewHolder viewHolder) {
        if (currentSwipedHolder != null && currentSwipedHolder != viewHolder) {
            closeSwipe(currentSwipedHolder);
        }
        View foreground = viewHolder.itemView.findViewById(R.id.layoutForeground);
        View background = viewHolder.itemView.findViewById(R.id.layoutBackground);
        background.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(foreground, "translationX", -250f).setDuration(200).start();
        currentSwipedHolder = viewHolder;
    }

    private void closeSwipe(RecyclerView.ViewHolder viewHolder) {
        View foreground = viewHolder.itemView.findViewById(R.id.layoutForeground);
        View background = viewHolder.itemView.findViewById(R.id.layoutBackground);
        background.setVisibility(View.GONE);
        ObjectAnimator.ofFloat(foreground, "translationX", 0f).setDuration(200).start();
        if (currentSwipedHolder == viewHolder) {
            currentSwipedHolder = null;
        }
    }

    private void editPassword(PasswordModel passwordModel) {
        Intent intent = new Intent(requireContext(), AddPasswordActivity.class);
        intent.putExtra("timeStamp", passwordModel.getTimeStamp());
        intent.putExtra("isEdit", true);
        createPasswordLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final ActivityResultLauncher<Intent> createPasswordLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    boolean isPasswordSaved = result.getData().getBooleanExtra("isSaved", false);
                    boolean isPasswordUpdated = result.getData().getBooleanExtra("isUpdated", false);
                    PasswordModel newPassword = (PasswordModel) result.getData().getSerializableExtra("newPassword");
                    if (isPasswordSaved && newPassword != null) {

                        if (passwordEntityCommonRecyclerViewAdapter != null) {
                            passwordEntityCommonRecyclerViewAdapter.addItemAtTop(newPassword);
                            passwordEntityCommonRecyclerViewAdapter.notifyItemInserted(0);
                            rvPasswordList.scrollToPosition(0); // optional: auto-scroll to top
                        } else {
                            fetchPasswords();
                        }

                        Toast.makeText(requireContext(), getString(R.string.password_saved_successfully), Toast.LENGTH_SHORT).show();
                    } else if (isPasswordUpdated) {
                        passwordEntityCommonRecyclerViewAdapter.updateData(passwordViewModel.getPasswords());
                        Toast.makeText(requireContext(), getString(R.string.password_updated_successfully), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void applyCombinedFilter() {
        String searchText = Objects.requireNonNull(etSearch.getText()).toString().trim().toLowerCase();

        List<PasswordModel> source = passwordViewModel.getPasswordsFavorites();
        List<PasswordModel> result = new ArrayList<>();

        for (PasswordModel model : source) {

            boolean matchesSearch =
                    model.getApplicationName().toLowerCase().contains(searchText) ||
                            model.getUserName().toLowerCase().contains(searchText);

            boolean matchesStrength =
                    (selectedStrength == -1) ||  // nothing selected → allow all
                            (model.getPasswordStrength() == selectedStrength);

            if (matchesSearch && matchesStrength) {
                result.add(model);
            }
        }

        if (result.isEmpty()) {
            tvNoDataFound.setVisibility(View.VISIBLE);
        } else {
            tvNoDataFound.setVisibility(View.GONE);
        }

        passwordEntityCommonRecyclerViewAdapter.updateData(result);
    }
}