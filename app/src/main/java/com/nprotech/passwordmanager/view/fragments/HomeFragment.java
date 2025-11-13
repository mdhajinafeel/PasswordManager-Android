package com.nprotech.passwordmanager.view.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.helper.CryptoHelper;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.view.activities.AddPasswordActivity;
import com.nprotech.passwordmanager.view.adapter.CommonRecyclerViewAdapter;
import com.nprotech.passwordmanager.view.adapter.ViewHolder;
import com.nprotech.passwordmanager.viewmodel.PasswordViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private RecyclerView rvPasswordList;
    private PasswordViewModel passwordViewModel;
    private CommonRecyclerViewAdapter<PasswordModel> passwordEntityCommonRecyclerViewAdapter;
    private RecyclerView.ViewHolder currentSwipedHolder;

    public static HomeFragment getInstance() {
        return new HomeFragment();
    }

    public static Bundle getStoredBundleValue(String title) {
        Bundle bundleOBJ = new Bundle();
        try {
            bundleOBJ.putString("Title", title);
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error getStoredBundleValue", e);
        }
        return bundleOBJ;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        try {
            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            AppCompatImageView ivAddPassword = view.findViewById(R.id.ivAddPassword);
            rvPasswordList = view.findViewById(R.id.rvPasswordList);

            passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

            txtTitle.setText(getString(R.string.app_name));

            ivAddPassword.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddPasswordActivity.class);
                intent.putExtra("isEdit", false);
                createPasswordLauncher.launch(intent);
            });

            fetchPasswords();
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error onCreateView", e);
        }

        return view;
    }

    private void fetchPasswords() {
        try {

            List<PasswordModel> passwordList = passwordViewModel.getPasswords();
            if (!passwordList.isEmpty()) {
                passwordEntityCommonRecyclerViewAdapter = new CommonRecyclerViewAdapter<>(requireContext(), passwordList,
                        R.layout.row_item_passwords) {
                    @Override
                    public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull PasswordModel item) {
                        try {
                            holder.setViewText(R.id.tvApplicationName, item.getApplicationName());
                            holder.setViewText(R.id.tvUsername, item.getUserName());
                            holder.setViewText(R.id.tvPassword, CryptoHelper.decrypt(CommonUtils.getPasswordAlias(), item.getPassword()));

                            if(item.getIcon() != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(item.getIcon(), 0, item.getIcon().length);
                                holder.setViewImageBitmap(R.id.imgPasswordIcon, bitmap);
                            }

                            AppCompatCheckBox cbFavorite = holder.getView(R.id.cbFavorite);
                            cbFavorite.setChecked(item.isFavourite());
                            cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) ->
                                    passwordViewModel.updateFavourite(item.getTimeStamp(), isChecked));

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
                setupSwipeToReveal();
            }
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error fetchPasswords", e);
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

    // ✅ Register Activity Result Launcher
    @SuppressLint("NotifyDataSetChanged")
    private final ActivityResultLauncher<Intent> createPasswordLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    boolean isPasswordSaved = result.getData().getBooleanExtra("isSaved", false);
                    boolean isPasswordUpdated = result.getData().getBooleanExtra("isUpdated", false);
                    PasswordModel newPassword = (PasswordModel) result.getData().getSerializableExtra("newPassword");
                    if (isPasswordSaved && newPassword != null) {
                        passwordEntityCommonRecyclerViewAdapter.addItem(newPassword);
                        int position = passwordEntityCommonRecyclerViewAdapter.getItemCount() - 1;
                        passwordEntityCommonRecyclerViewAdapter.notifyItemInserted(position);
                        Toast.makeText(requireContext(), getString(R.string.password_saved_successfully), Toast.LENGTH_LONG).show();
                    } else if(isPasswordUpdated) {
                        passwordEntityCommonRecyclerViewAdapter.updateData(passwordViewModel.getPasswords());
                        Toast.makeText(requireContext(), getString(R.string.password_updated_successfully), Toast.LENGTH_LONG).show();
                    }
                }
            });
}