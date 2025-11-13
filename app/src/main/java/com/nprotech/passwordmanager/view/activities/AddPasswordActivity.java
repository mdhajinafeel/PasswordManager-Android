package com.nprotech.passwordmanager.view.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.db.entities.IconEntity;
import com.nprotech.passwordmanager.db.entities.PasswordEntity;
import com.nprotech.passwordmanager.helper.CryptoHelper;
import com.nprotech.passwordmanager.model.PasswordModel;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.view.adapter.CategoryAdapter;
import com.nprotech.passwordmanager.view.adapter.CommonRecyclerViewAdapter;
import com.nprotech.passwordmanager.view.adapter.ViewHolder;
import com.nprotech.passwordmanager.viewmodel.MasterViewModel;
import com.nprotech.passwordmanager.viewmodel.PasswordViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddPasswordActivity extends BaseActivity implements View.OnClickListener {

    private TextInputEditText etApplicationName, etUserName, etApplicationLink, etPassword;
    private TextInputLayout tiApplicationName, tiUserName, tiCategory, tiPassword;
    private AppCompatTextView txtPasswordStrength;
    private ShapeableImageView imgPasswordIcon;
    private View passwordStrengthBar;
    private MaterialAutoCompleteTextView etCategory;
    private MaterialCheckBox cbNumbers, cbSymbols, cbLowerCase, cbUpperCase;
    private Slider lengthSlider;
    private FrameLayout progressBar;
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_-+=<>?/{}~|";
    private MasterViewModel masterViewModel;
    private PasswordViewModel passwordViewModel;
    private int categoryId, iconId;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private byte[] imgPasswordIconArray;
    private boolean isCustomIcon;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_password);

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

            etApplicationName = findViewById(R.id.etApplicationName);
            etUserName = findViewById(R.id.etUserName);
            etApplicationLink = findViewById(R.id.etApplicationLink);
            etCategory = findViewById(R.id.etCategory);
            etPassword = findViewById(R.id.etPassword);
            imgPasswordIcon = findViewById(R.id.imgPasswordIcon);
            AppCompatTextView tvChangeIcon = findViewById(R.id.tvChangeIcon);

            passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
            txtPasswordStrength = findViewById(R.id.txtPasswordStrength);

            tiApplicationName = findViewById(R.id.tiApplicationName);
            tiUserName = findViewById(R.id.tiUserName);
            tiCategory = findViewById(R.id.tiCategory);
            tiPassword = findViewById(R.id.tiPassword);

            cbNumbers = findViewById(R.id.cbNumbers);
            cbSymbols = findViewById(R.id.cbSymbols);
            cbLowerCase = findViewById(R.id.cbLowerCase);
            cbUpperCase = findViewById(R.id.cbUpperCase);

            lengthSlider = findViewById(R.id.lengthSlider);
            progressBar = findViewById(R.id.progressBar);

            MaterialButton btnGenerate = findViewById(R.id.btnGenerate);
            MaterialButton btnSave = findViewById(R.id.btnSave);

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

            bundle = getIntent().getExtras();

            if (bundle != null) {

                if(bundle.getBoolean("isEdit")) {
                    txtTitle.setText(R.string.update_password);
                    btnSave.setText(R.string.update);
                } else {
                    txtTitle.setText(R.string.add_password);
                    btnSave.setText(R.string.save);
                }

                icBack.setOnClickListener(view -> finish());

                // Set initial thumb
                lengthSlider.setCustomThumbDrawable(createThumbWithText((int) lengthSlider.getValue()));

                lengthSlider.addOnChangeListener((slider, value, fromUser) -> {
                    // Update thumb with new value
                    slider.setCustomThumbDrawable(createThumbWithText((int) value));
                });

                btnGenerate.setOnClickListener(this);
                btnSave.setOnClickListener(this);

                passwordViewModel.getProgressState().observe(this, isProgress -> {
                    if (isProgress) {
                        showProgress(progressBar);
                    } else {
                        hideProgress(progressBar);
                    }
                });

                etPassword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updatePasswordStrength(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                tvChangeIcon.setOnClickListener(view -> showIconsDialog());

                imagePickerLauncher = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Uri imageUri = result.getData().getData();
                                handleSelectedImage(imageUri);
                                isCustomIcon = true;
                            }
                        }
                );

                imgPasswordIcon.setClipToOutline(true);
                imgPasswordIcon.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        int diameter = Math.min(view.getWidth(), view.getHeight());
                        outline.setOval(0, 0, diameter, diameter);
                    }
                });

                fetchEditData(bundle);
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isSaved", false); // optional data
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error initComponents", e);
        }
    }

    private void fetchAllCategories() {

        List<CategoryEntity> categoryEntities = masterViewModel.getAllCategories();

        if (categoryEntities != null && !categoryEntities.isEmpty()) {
            CategoryAdapter categoryAdapter = new CategoryAdapter(this, categoryEntities);
            etCategory.setAdapter(categoryAdapter);

            // Always show dropdown when clicked
            etCategory.setOnClickListener(v -> {
                if (!etCategory.isPopupShowing()) {
                    etCategory.showDropDown();
                }
            });

            // Show dropdown when focused
            etCategory.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    etCategory.showDropDown();
                }
            });

            // Handle selection
            etCategory.setOnItemClickListener((parent, view, position, id) -> {
                CategoryEntity selected = (CategoryEntity) parent.getItemAtPosition(position);
                categoryId = selected.getId();
                etCategory.setText(selected.getCategoryName(), false); // ← prevents filtering issue
            });
        }
    }

    private void fetchEditData(Bundle bundle) {
        try {
            if(bundle.getBoolean("isEdit")) {

                PasswordEntity passwordEntity = passwordViewModel.getPassword(bundle.getLong("timeStamp"));

                if(passwordEntity != null) {
                    etApplicationName.setText(passwordEntity.getApplicationName());
                    etUserName.setText(passwordEntity.getApplicationName());
                    etApplicationLink.setText(passwordEntity.getLink());
                    etPassword.setText(CryptoHelper.decrypt(CommonUtils.getPasswordAlias(), passwordEntity.getPassword()));
                    isCustomIcon = passwordEntity.isCustomIcon();
                    categoryId = passwordEntity.getCategory();
                    iconId = passwordEntity.getIconId();

                    PasswordModel passwordModel = passwordViewModel.getPasswordModel(bundle.getLong("timeStamp"));
                    if(passwordModel != null) {

                        etCategory.setText(passwordModel.getCategory());

                        Bitmap bitmap = BitmapFactory.decodeByteArray(passwordModel.getIcon(), 0, passwordModel.getIcon().length);
                        imgPasswordIcon.setImageBitmap(bitmap);

                        if(passwordModel.isCustomIcon()) {
                            imgPasswordIconArray = passwordModel.getIcon();
                        } else {
                            imgPasswordIconArray = null;
                        }
                    }
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("isSaved", false); // optional data
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }

            fetchAllCategories();
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error fetchEditData", e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnGenerate) {
            generatePassword();
        } else if (view.getId() == R.id.btnSave) {
            savePassword();
        }
    }

    private Drawable createThumbWithText(int value) {
        int size = 100; // px size of thumb
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        try {

            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // Draw circle
            paint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

            // Draw text
            paint.setColor(Color.WHITE);
            paint.setTextSize(40f);
            paint.setTextAlign(Paint.Align.CENTER);
            float yPos = (canvas.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);
            canvas.drawText(String.valueOf(value), size / 2f, yPos, paint);
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error createThumbWithText", e);
        }

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void generatePassword() {
        try {
            int length = (int) lengthSlider.getValue();
            StringBuilder characterPool = new StringBuilder();

            if (cbLowerCase.isChecked()) characterPool.append(LOWERCASE);
            if (cbUpperCase.isChecked()) characterPool.append(UPPERCASE);
            if (cbNumbers.isChecked()) characterPool.append(NUMBERS);
            if (cbSymbols.isChecked()) characterPool.append(SYMBOLS);

            if (characterPool.length() == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.please_select_at_least_one_option), Toast.LENGTH_SHORT).show();
                return;
            }

            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder();

            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characterPool.length());
                password.append(characterPool.charAt(index));
            }

            etPassword.setText(password.toString());
            updatePasswordStrength(password.toString());
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error generatePassword", e);
        }
    }

    private void savePassword() {
        try {
            boolean isValid1 = true, isValid2 = true, isValid3 = true, isValid4 = true;

            if (Objects.requireNonNull(etApplicationName.getText()).toString().isEmpty()) {
                tiApplicationName.setError(getString(R.string.required_field));
                tiApplicationName.setErrorEnabled(true);
                isValid1 = false;
            } else {
                tiApplicationName.setError(null);
                tiApplicationName.setErrorEnabled(false);
            }

            if (Objects.requireNonNull(etUserName.getText()).toString().isEmpty()) {
                tiUserName.setError(getString(R.string.required_field));
                tiUserName.setErrorEnabled(true);
                isValid2 = false;
            } else {
                tiUserName.setError(null);
                tiUserName.setErrorEnabled(false);
            }

            if (Objects.requireNonNull(etCategory.getText()).toString().isEmpty()) {
                tiCategory.setError(getString(R.string.required_field));
                tiCategory.setErrorEnabled(true);
                isValid3 = false;
            } else {
                tiCategory.setError(null);
                tiCategory.setErrorEnabled(false);
            }

            if (Objects.requireNonNull(etPassword.getText()).toString().isEmpty()) {
                tiPassword.setError(getString(R.string.required_field));
                tiPassword.setErrorEnabled(true);
                isValid4 = false;
            } else {
                tiPassword.setError(null);
                tiPassword.setErrorEnabled(false);
            }

            if (isValid1 && isValid2 && isValid3 && isValid4) {

                PasswordEntity passwordEntity = new PasswordEntity();
                passwordEntity.setApplicationName(etApplicationName.getText().toString());
                passwordEntity.setUserName(Objects.requireNonNull(etUserName.getText()).toString());
                passwordEntity.setLink(Objects.requireNonNull(etApplicationLink.getText()).toString());
                passwordEntity.setCategory(categoryId);
                passwordEntity.setPassword(CryptoHelper.encrypt(CommonUtils.getPasswordAlias(), etPassword.getText().toString()));
                passwordEntity.setSynced(false);
                passwordEntity.setIcon(imgPasswordIconArray);
                passwordEntity.setCustomIcon(isCustomIcon);
                passwordEntity.setIconId(iconId);

                if(bundle.getBoolean("isEdit")) {

                    passwordEntity.setTimeStamp(bundle.getLong("timeStamp"));

                    passwordViewModel.updatePassword(passwordEntity);

                    passwordViewModel.getUpdateStatus().observe(this, s -> {
                        if (s) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("isSaved", false);
                            resultIntent.putExtra("isUpdated", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            showCustomDialog(passwordViewModel.getErrorTitle(), passwordViewModel.getErrorMessage(), false);
                        }
                    });
                } else {

                    passwordEntity.setDeleted(false);
                    passwordEntity.setFavourite(false);
                    passwordEntity.setDatabaseId(0);
                    passwordEntity.setTimeStamp(CommonUtils.getCurrentDateTimeStamp(true));

                    passwordViewModel.savePassword(passwordEntity);

                    passwordViewModel.getSaveStatus().observe(this, s -> {
                        if (s) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("newPassword", passwordEntity);
                            resultIntent.putExtra("isSaved", true);
                            resultIntent.putExtra("isUpdated", false);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            showCustomDialog(passwordViewModel.getErrorTitle(), passwordViewModel.getErrorMessage(), false);
                        }
                    });
                }
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error savePassword", e);
        }
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);
        updateStrengthView(strength);
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;

        if (password.isEmpty()) {
            return -100;
        } else {
            if (password.length() >= 8) score++;
            if (password.length() >= 12) score++;
            if (password.matches(".*[0-9].*")) score++;
            if (password.matches(".*[a-z].*")) score++;
            if (password.matches(".*[A-Z].*")) score++;
            if (password.matches(".*[!@#$%^&*()_+\\-=<>?{}\\[\\]~|].*")) score++;
        }

        return Math.min(score, 5); // 0–5 scale
    }

    private void updateStrengthView(int strength) {
        int color;
        String label;
        float widthPercent;

        if (strength < 0) {
            txtPasswordStrength.setVisibility(View.GONE);

            passwordStrengthBar.animate()
                    .scaleX(0)
                    .setDuration(300)
                    .start();
            passwordStrengthBar.setPivotX(0f); // make scaling start from the left
            passwordStrengthBar.setScaleX(0f); // start hidden (0% width)
        } else {

            switch (strength) {
                case 0:
                case 1:
                    color = ContextCompat.getColor(this, R.color.weakColor);
                    label = getString(R.string.weak);
                    widthPercent = 0.25f;
                    break;
                case 2:
                case 3:
                    color = ContextCompat.getColor(this, R.color.mediumColor);
                    label = getString(R.string.medium);
                    widthPercent = 0.5f;
                    break;
                case 4:
                    color = ContextCompat.getColor(this, R.color.strongColor);
                    label = getString(R.string.strong);
                    widthPercent = 0.75f;
                    break;
                default:
                    color = ContextCompat.getColor(this, R.color.veryStrongColor);
                    label = getString(R.string.very_strong);
                    widthPercent = 1f;
                    break;
            }

            // Animate bar width
            passwordStrengthBar.animate()
                    .scaleX(widthPercent)
                    .setDuration(300)
                    .start();
            passwordStrengthBar.setPivotX(0f); // make scaling start from the left
            passwordStrengthBar.setScaleX(0f); // start hidden (0% width)

            txtPasswordStrength.setText(String.format("%s%s", getString(R.string.strength), label));
            txtPasswordStrength.setTextColor(color);
            txtPasswordStrength.setVisibility(View.VISIBLE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Compress image
            Bitmap compressed = compressBitmap(bitmap);

            // Convert to byte[]
            byte[] imageBytes = bitmapToByteArray(compressed);

            // Show preview
            imgPasswordIcon.setImageBitmap(compressed);

            imgPasswordIconArray = imageBytes;

        } catch (IOException e) {
            AppLogger.e(getClass(), "Error handleSelectedImage", e);
        }
    }

    private Bitmap compressBitmap(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratio = (float) width / height;
        if (width > 800 || height > 800) {
            if (ratio > 1) {
                width = 800;
                height = (int) (width / ratio);
            } else {
                height = 800;
                width = (int) (height * ratio);
            }
        }

        Bitmap scaled = Bitmap.createScaledBitmap(original, width, height, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.PNG, 90, out);
        byte[] bytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        return stream.toByteArray();
    }

    private void showIconsDialog() {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View dialogView = dialogInflater.inflate(R.layout.dialog_select_icons, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatImageView ivDialogClose = dialogView.findViewById(R.id.ivDialogClose);
            AppCompatEditText etDialogSearch = dialogView.findViewById(R.id.etDialogSearch);
            RecyclerView rvDialogIcons = dialogView.findViewById(R.id.rvDialogIcons);
            AppCompatTextView tvDialogCustomIcons = dialogView.findViewById(R.id.tvDialogCustomIcons);

            ivDialogClose.setOnClickListener(v -> dialog.dismiss());
            tvDialogCustomIcons.setOnClickListener(v -> {
                dialog.dismiss();
                openImagePicker();
            });

            if (!masterViewModel.getAllIcons().isEmpty()) {
                etDialogSearch.setVisibility(View.VISIBLE);
                rvDialogIcons.setVisibility(View.VISIBLE);

                CommonRecyclerViewAdapter<IconEntity> iconCommonRecyclerViewAdapter = new CommonRecyclerViewAdapter<>(getApplicationContext(), masterViewModel.getAllIcons(),
                        R.layout.row_item_icon) {
                    @Override
                    public void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull IconEntity item) {
                        holder.setViewText(R.id.tvIconName, item.getName());

                        Bitmap bitmapIcon = BitmapFactory.decodeByteArray(item.getIcon(), 0, item.getIcon().length);
                        holder.setViewImageBitmap(R.id.ivIcon, bitmapIcon);

                        holder.getView(R.id.llIcon).setOnClickListener(v -> {
                            iconId = item.getId();
                            imgPasswordIcon.setImageBitmap(bitmapIcon);
                            isCustomIcon = false;
                            dialog.dismiss();
                        });
                    }
                };

                rvDialogIcons.setAdapter(iconCommonRecyclerViewAdapter);
                rvDialogIcons.setNestedScrollingEnabled(true);

            } else {
                etDialogSearch.setVisibility(View.GONE);
                rvDialogIcons.setVisibility(View.GONE);
            }

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error showIconsDialog", e);
        }
    }
}