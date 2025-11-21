package com.nprotech.passwordmanager.view.activities;

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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.helper.CryptoHelper;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.view.adapter.CommonRecyclerViewAdapter;
import com.nprotech.passwordmanager.view.adapter.ViewHolder;
import com.nprotech.passwordmanager.viewmodel.MasterViewModel;
import com.nprotech.passwordmanager.viewmodel.PasswordViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryPasswordActivity extends BaseActivity {

    private PasswordViewModel passwordViewModel;
    private RecyclerView rvPasswordList;
    private FrameLayout frameNoData;
    private CommonRecyclerViewAdapter<PasswordModel> passwordEntityCommonRecyclerViewAdapter;
    private RecyclerView.ViewHolder currentSwipedHolder;
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_category_passwords);

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            initComponents();

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error onCreate", e);
        }
    }

    private void initComponents() {
        try {

            hideKeyboard(this);

            AppCompatTextView txtTitle = findViewById(R.id.txtTitle);
            AppCompatImageView icBack = findViewById(R.id.icBack);
            rvPasswordList = findViewById(R.id.rvPasswordList);
            frameNoData = findViewById(R.id.frameNoData);

            MasterViewModel masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                categoryId = bundle.getInt("categoryId");

                CategoryEntity categoryEntity = masterViewModel.getCategoryById(categoryId);
                List<PasswordModel> passwordList = passwordViewModel.getPasswordsByCategory(categoryId);

                txtTitle.setText(categoryEntity.getCategoryName());

                icBack.setOnClickListener(v -> finish());

                fetchPasswordsByCategory(passwordList);
            } else {
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error initComponents", e);
        }
    }

    private void fetchPasswordsByCategory(List<PasswordModel> passwordList) {
        try {
            if (!passwordList.isEmpty()) {
                passwordEntityCommonRecyclerViewAdapter = new CommonRecyclerViewAdapter<>(this, passwordList,
                        R.layout.row_item_passwords) {
                    @Override
                    public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull PasswordModel item) {
                        try {

                            holder.setViewText(R.id.tvApplicationName, item.getApplicationName());
                            holder.setViewText(R.id.tvUsername, item.getUserName());

                            String decryptedPassword = CryptoHelper.decrypt(CommonUtils.getPasswordAlias(), item.getPassword());
                            AppCompatTextView tvPassword = holder.getView(R.id.tvPassword);
                            tvPassword.setText("••••••••");

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
                            cbFavorite.setChecked(item.isFavourite());
                            cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) ->
                                    passwordViewModel.updateFavourite(item.getTimeStamp(), isChecked));

                            // Copy Password
                            holder.getView(R.id.ivCopyPassword).setOnClickListener(v -> {
                                ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("password", decryptedPassword);
                                clipboard.setPrimaryClip(clip);

                                Toast.makeText(getApplicationContext(), "Password copied", Toast.LENGTH_SHORT).show();
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
                                        ContextCompat.getColor(getApplicationContext(), colorRes)
                                );
                            }

                            // Set text + text color
                            tvStrengthLabel.setText(getString(textRes));
                            tvStrengthLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), colorRes));

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
                            AppLogger.e(getClass(), "Error Password List", e);
                        }

                    }
                };

                rvPasswordList.setAdapter(passwordEntityCommonRecyclerViewAdapter);
                rvPasswordList.setVisibility(View.VISIBLE);
                frameNoData.setVisibility(View.GONE);
                setupSwipeToReveal();
            } else {
                rvPasswordList.setVisibility(View.GONE);
                frameNoData.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error fetchPasswordsByCategory", e);
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
        Intent intent = new Intent(this, AddPasswordActivity.class);
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

                        if (passwordEntityCommonRecyclerViewAdapter != null) {
                            passwordEntityCommonRecyclerViewAdapter.addItemAtTop(newPassword);
                            passwordEntityCommonRecyclerViewAdapter.notifyItemInserted(0);
                            rvPasswordList.scrollToPosition(0); // optional: auto-scroll to top
                        } else {
                            List<PasswordModel> passwordList = passwordViewModel.getPasswordsByCategory(categoryId);
                            fetchPasswordsByCategory(passwordList);
                        }

                        Toast.makeText(getApplicationContext(), getString(R.string.password_saved_successfully), Toast.LENGTH_SHORT).show();
                    } else if (isPasswordUpdated) {
                        passwordEntityCommonRecyclerViewAdapter.updateData(passwordViewModel.getPasswords());
                        Toast.makeText(getApplicationContext(), getString(R.string.password_updated_successfully), Toast.LENGTH_SHORT).show();
                    }
                }
            });
}