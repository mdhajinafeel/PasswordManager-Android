package com.nprotech.passwordmanager.view.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.helper.PreferenceManager;

import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    private boolean isAuthenticating = false;
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        hideKeyboard(this);

        // Intercept Back Press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

        // Delay and move to next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (PreferenceManager.INSTANCE.getLoggedIn() && PreferenceManager.INSTANCE.getBioMetric()) {
                showBiometricPrompt();
            } else if (PreferenceManager.INSTANCE.getLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class)
                        .putExtra("isFromLogin", false)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        }, SPLASH_DELAY);
    }

    private void showBiometricPrompt() {
        if (isAuthenticating) return;
        isAuthenticating = true;

        Executor executor = ContextCompat.getMainExecutor(this);

        // Proceed to dashboard
        // User pressed back / tapped outside
        // show dialog immediately instead of reopening
        // User pressed back / tapped outside
        // show dialog immediately instead of reopening
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isAuthenticating = false;

                        // Proceed to dashboard
                        startActivity(new Intent(SplashActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        isAuthenticating = false;

                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            // User pressed back / tapped outside
                            showExitConfirmationDialog(); // show dialog immediately instead of reopening
                        } else if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_CANCELED) {
                            // User pressed back / tapped outside
                            showExitConfirmationDialog(); // show dialog immediately instead of reopening
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(SplashActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.unlock_with_biometrics))
                .setSubtitle(getString(R.string.use_your_fingerprint_or_face_to_continue))
                .setNegativeButtonText(getString(R.string.text_cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PreferenceManager.INSTANCE.getLoggedIn() && PreferenceManager.INSTANCE.getBioMetric()) {
            new Handler(Looper.getMainLooper()).postDelayed(this::showBiometricPrompt, 100);
        }
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);
        builder.setView(customLayout);
        builder.setCancelable(false); // Prevent dismiss on outside tap

        final AlertDialog dialog = builder.create();

        // Find views
        AppCompatTextView tvCancel = customLayout.findViewById(R.id.tvCancel);
        AppCompatTextView tvUnlock = customLayout.findViewById(R.id.tvUnlock);

        tvUnlock.setOnClickListener(v -> {
            dialog.dismiss();
            showBiometricPrompt(); // reopen biometric
        });

        tvCancel.setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity(); // close app
        });

        dialog.show();
    }
}
