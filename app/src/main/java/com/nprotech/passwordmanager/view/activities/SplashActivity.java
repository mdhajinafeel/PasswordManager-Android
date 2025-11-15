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
import androidx.biometric.BiometricManager;
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
    private static final int SPLASH_DELAY = 1500; // 1.5 seconds
    private boolean biometricShown = false;
    private static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP;

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

            boolean isLoggedIn = PreferenceManager.INSTANCE.getLoggedIn();
            boolean isRememberMe = PreferenceManager.INSTANCE.getRememberMe();
            boolean isGoogleSignIn = PreferenceManager.INSTANCE.getGoogleSignIn();
            boolean isBiometric = PreferenceManager.INSTANCE.getBioMetric();
            boolean isAutoLogin = isRememberMe || isGoogleSignIn;

            if (isLoggedIn) {
                if (isBiometric && isAutoLogin) {
                    showBiometricPrompt();
                } else if (isAutoLogin) {
                    redirectToMain();
                } else {
                    redirectToLogin();
                }
            } else {
                redirectToLogin();
            }
        }, SPLASH_DELAY);
    }

    private void redirectToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .putExtra("isFromLogin", false)
                .addFlags(INTENT_FLAGS));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(INTENT_FLAGS));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showBiometricPrompt() {

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            redirectToMain(); // fallback if no biometric available
            return;
        }

        if (isAuthenticating) return;
        isAuthenticating = true;

        Executor executor = ContextCompat.getMainExecutor(this);

        // Initialize biometric prompt and handle authentication callbacks
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isAuthenticating = false;

                        // Proceed to dashboard
                        redirectToMain();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        isAuthenticating = false;

                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_CANCELED) {
                            showExitConfirmationDialog();
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
        if (isFinishing() || isAuthenticating) return;

        boolean shouldPrompt = PreferenceManager.INSTANCE.getLoggedIn()
                && PreferenceManager.INSTANCE.getBioMetric()
                && (PreferenceManager.INSTANCE.getRememberMe() || PreferenceManager.INSTANCE.getGoogleSignIn());

        if (shouldPrompt && !biometricShown) {
            biometricShown = true;
            isAuthenticating = true;
            new Handler(Looper.getMainLooper()).postDelayed(this::showBiometricPrompt, 200);
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