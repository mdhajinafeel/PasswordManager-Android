package com.nprotech.passwordmanager.view.fragments;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.material.button.MaterialButton;
import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.model.SettingItem;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.SimpleDividerItemDecoration;
import com.nprotech.passwordmanager.view.activities.MainActivity;
import com.nprotech.passwordmanager.view.adapter.SettingsRecyclerAdapter;
import com.nprotech.passwordmanager.viewmodel.AuthViewModel;
import com.nprotech.passwordmanager.viewmodel.MasterViewModel;
import com.nprotech.passwordmanager.work.SyncScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements SettingsRecyclerAdapter.OnSettingActionListener {

    private RecyclerView settingsListView;
    private List<SettingItem> settingItems;
    private SettingsRecyclerAdapter adapter;
    private AuthViewModel loginViewModel;
    private MasterViewModel masterViewModel;
    // Permission launcher (Android 13+)
    //private ActivityResultLauncher<String> notificationPermissionLauncher;

    public static SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        try {

            AppCompatImageView profileImage = view.findViewById(R.id.profileImage);
            AppCompatTextView tvName = view.findViewById(R.id.tvName);
            AppCompatTextView tvEmail = view.findViewById(R.id.tvEmail);
            settingsListView = view.findViewById(R.id.settingsListView);
            MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
            AppCompatTextView tvAppVersion = view.findViewById(R.id.tvAppVersion);

            loginViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);
            txtTitle.setText(getString(R.string.settings));

            tvName.setText(PreferenceManager.INSTANCE.getName());
            tvEmail.setText(PreferenceManager.INSTANCE.getEmail());

            // Create and set the image
            new Thread(() -> {
                Bitmap avatar = createInitialsBitmap(getInitials(PreferenceManager.INSTANCE.getName()), ContextCompat.getColor(requireContext(), R.color.colorLightBlue));
                requireActivity().runOnUiThread(() -> profileImage.setImageBitmap(avatar));
            }).start();

            //VERSION
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);

            String versionName = pInfo.versionName;
            long versionCode;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = pInfo.getLongVersionCode();
            } else {
                versionCode = pInfo.versionCode;
            }

            tvAppVersion.setText(getString(R.string.app_version, versionName, versionCode));

            loginViewModel.getProgressState().observe(getViewLifecycleOwner(), isProgress -> {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    if (isProgress) {
                        mainActivity.showProgress();  // call showProgress
                    } else {
                        mainActivity.hideProgress();  // call hideProgress
                    }
                }
            });

            masterViewModel.getProgressState().observe(getViewLifecycleOwner(), isProgress -> {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    if (isProgress) {
                        mainActivity.showProgress();  // call showProgress
                    } else {
                        mainActivity.hideProgress();  // call hideProgress
                    }
                }
            });

            //LOGOUT
            btnLogout.setOnClickListener(view1 -> performLogout());

            //FETCH SETTINGS
            settingsListView.post(this::fetchSettings);
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error onCreateView", e);
        }

        return view;
    }

    private void fetchSettings() {
        try {
            settingItems = new ArrayList<>();
            settingItems.add(new SettingItem(1, R.drawable.ic_fingerprint, getString(R.string.enable_biometric), true, false, false));
            settingItems.add(new SettingItem(2, R.drawable.ic_dark_theme, getString(R.string.enable_dark_theme), true, false, false));
            settingItems.add(new SettingItem(3, R.drawable.ic_download, getString(R.string.download_passwords), false, false, false));
            settingItems.add(new SettingItem(4, R.drawable.ic_timer, getString(R.string.download_sync_interval), false, false, true));

            if (PreferenceManager.INSTANCE.getSyncHours() == 0) {
                settingItems.add(new SettingItem(5, R.drawable.ic_sync, getString(R.string.synchronization), false, false, false));
            }

            settingItems.add(new SettingItem(6, R.drawable.ic_about, getString(R.string.about_app), false, false, false));

            adapter = new SettingsRecyclerAdapter(requireContext(), settingItems, this);
            settingsListView.setLayoutManager(new LinearLayoutManager(requireContext()));
            settingsListView.addItemDecoration(new SimpleDividerItemDecoration(requireContext()));
            settingsListView.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error fetchSettings", e);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            initials.append(Character.toUpperCase(part.charAt(0)));
        }
        return initials.toString();
    }

    private Bitmap createInitialsBitmap(String initials, int bgColor) {
        int size = 200; // Fixed bitmap size
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw background circle
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(bgColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint);

        // Configure text paint
        Paint textPaint = createTextPaint(initials, size);

        // ✅ Adjust baseline vertically (centering fix)
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float x = size / 2f;
        float y = (size / 2f) - (fm.ascent + fm.descent) / 2f;

        // ✅ Now draw the text (make sure initials not empty)
        if (initials != null && !initials.isEmpty()) {
            canvas.drawText(initials, x, y, textPaint);
        }

        return bitmap;
    }

    // Extracted method
    private Paint createTextPaint(String text, int canvasSize) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);

        // Dynamically adjust text size to fit inside the circle
        float textSize = canvasSize / 2f; // Start large
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(text);
        while (textWidth > canvasSize * 0.8f) { // leave 10% padding
            textSize -= 2f;
            paint.setTextSize(textSize);
            textWidth = paint.measureText(text);
        }

        return paint;
    }

    @Override
    public void onSettingClick(SettingItem item, int itemId, int itemValue) {
        if (item.getSettingId() == 4) {
            if (itemId > 0) {
                PreferenceManager.INSTANCE.setSyncId(itemId);
                PreferenceManager.INSTANCE.setSyncHours(itemValue);

                if (itemValue == 0) {
                    SyncScheduler.cancelHourlySync(requireContext());
                } else {
                    SyncScheduler.scheduleHourlySync(requireContext(), PreferenceManager.INSTANCE.getSyncHours());
                }

                // ✅ Dynamically add/remove "Synchronization" item
                toggleSyncItem(itemValue == 0);
            }
        } else if (item.getSettingId() == 5) {
            if (itemId == 0) {
                masterViewModel.masterDownload();

                masterViewModel.getErrorMessage().observe(this, s -> ErrorDialogFragment.newInstance(
                        masterViewModel.getErrorTitle().getValue(),
                        masterViewModel.getErrorMessage().getValue(),
                        false
                ).show(getChildFragmentManager(), "errorDialog"));
            }
        }
    }

    @Override
    public void onSwitchToggle(SettingItem item, boolean isChecked, LabeledSwitch switchButton) {
        if (item.getSettingId() == 1) {
            if (isChecked) {
                enableBiometricAuth(switchButton);
            } else {
                disableBiometricAuth(switchButton);
            }
        } else if (item.getSettingId() == 2) {
            toggleDarkMode(isChecked, switchButton);
        }
    }

    //BIOMETRIC
    private void enableBiometricAuth(LabeledSwitch switchButton) {
        BiometricManager biometricManager = BiometricManager.from(requireContext());

        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPrompt(switchButton);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireContext(), getString(R.string.no_biometric_hardware_detected), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(requireContext(), getString(R.string.biometric_hardware_unavailable), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(requireContext(), getString(R.string.no_biometric_enrolled_on_this_device), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Toast.makeText(requireContext(), getString(R.string.security_update_required_to_use_biometrics), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Toast.makeText(requireContext(), getString(R.string.biometric_type_not_supported_on_this_device), Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Toast.makeText(requireContext(), getString(R.string.cannot_determine_biometric_status), Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(requireContext(), getString(R.string.biometric_status_unknown), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showBiometricPrompt(LabeledSwitch switchButton) {
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(requireContext(), R.string.biometric_enabled_successfully, Toast.LENGTH_SHORT).show();

                // Save preference
                PreferenceManager.INSTANCE.setBioMetric(true);

                switchButton.setOn(PreferenceManager.INSTANCE.getBioMetric());
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(requireContext(), getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.enable_biometric_unlock)).setSubtitle(getString(R.string.biometric_subtitle)).setNegativeButtonText(getString(R.string.text_cancel)).build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void disableBiometricAuth(LabeledSwitch switchButton) {
        PreferenceManager.INSTANCE.setBioMetric(false);
        switchButton.setOn(PreferenceManager.INSTANCE.getBioMetric());
        Toast.makeText(requireContext(), getString(R.string.biometric_disabled), Toast.LENGTH_SHORT).show();
    }

    //DARK MODE
    private void toggleDarkMode(boolean enable, LabeledSwitch switchButton) {
        AppCompatDelegate.setDefaultNightMode(enable ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        // Save preference
        PreferenceManager.INSTANCE.setDarkMode(enable);
        switchButton.setOn(PreferenceManager.INSTANCE.getDarkMode());
        Toast.makeText(requireContext(), getString(R.string.dark_mode) + (enable ? getString(R.string.enabled) : getString(R.string.disabled)), Toast.LENGTH_SHORT).show();
    }

    //LOGOUT
    private void performLogout() {
        try {
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
            View dialogView = dialogInflater.inflate(R.layout.custom_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            AppCompatTextView dialogHeader = dialogView.findViewById(R.id.dialogHeader);
            AppCompatTextView dialogBody = dialogView.findViewById(R.id.dialogBody);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            dialogHeader.setText(getString(R.string.logout));
            dialogBody.setText(getString(R.string.logout_confirmation));

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnOk.setOnClickListener(v -> {

                loginViewModel.logout();

                loginViewModel.getLogout().observe(requireActivity(), aBoolean -> {
                    dialog.dismiss();
                    if (aBoolean) {
                        if (getActivity() instanceof BaseActivity) {

                            PreferenceManager.INSTANCE.clearLoginSession();
                            SyncScheduler.cancelHourlySync(requireContext());
                            masterViewModel.clearScheduler();

                            ((BaseActivity) getActivity()).performLogout();
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error performLogout", e);
        }
    }

    private void toggleSyncItem(boolean show) {
        if (adapter == null || settingItems == null) return;

        int syncItemIndex = -1;
        for (int i = 0; i < settingItems.size(); i++) {
            if (settingItems.get(i).getSettingId() == 5) {
                syncItemIndex = i;
                break;
            }
        }

        if (show && syncItemIndex == -1) {
            // Find where Sync Interval (ID = 4) is located
            int insertAfterIndex = -1;
            for (int i = 0; i < settingItems.size(); i++) {
                if (settingItems.get(i).getSettingId() == 4) {
                    insertAfterIndex = i;
                    break;
                }
            }

            // Create new item
            SettingItem syncItem = new SettingItem(5, R.drawable.ic_sync, getString(R.string.synchronization), false, false, false);

            if (insertAfterIndex != -1 && insertAfterIndex < settingItems.size() - 1) {
                // Insert right after Sync Interval
                settingItems.add(insertAfterIndex + 1, syncItem);
                adapter.notifyItemInserted(insertAfterIndex + 1);
            } else {
                // Fallback — append at the end (should rarely happen)
                settingItems.add(syncItem);
                adapter.notifyItemInserted(settingItems.size() - 1);
            }

        } else if (!show && syncItemIndex != -1) {
            // Remove if exists
            settingItems.remove(syncItemIndex);
            adapter.notifyItemRemoved(syncItemIndex);
        }
    }
}