package com.nprotech.passwordmanager.view.activities;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.model.request.LoginRequest;
import com.nprotech.passwordmanager.model.request.SignUpRequest;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.utils.CommonUtils;
import com.nprotech.passwordmanager.utils.NetworkConnectivity;
import com.nprotech.passwordmanager.viewmodel.AuthViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@SuppressWarnings("deprecation")
@AndroidEntryPoint
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatTextView tabLogin, tabSignup;
    private LinearLayout loginForm, signupForm;
    private TextInputLayout tiEmailLogin, tiPasswordLogin, tiNameSignup, tiEmailSignup, tiPasswordSignup;
    private TextInputEditText etEmailLogin, etPasswordLogin, etNameSignup, etEmailSignup, etPasswordSignup;
    private FrameLayout progressBar;
    private Animation fadeIn, fadeOut;
    private Typeface typeFaceMedium, typeFaceBold;
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 1100;
    private String fcmToken = "";
    private AuthViewModel loginViewModel;
    private boolean isRememberMe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

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

            progressBar = findViewById(R.id.progress_bar);

            tabLogin = findViewById(R.id.tabLogin);
            tabSignup = findViewById(R.id.tabSignup);
            loginForm = findViewById(R.id.loginForm);
            signupForm = findViewById(R.id.signupForm);

            tiEmailLogin = findViewById(R.id.tiEmailLogin);
            tiPasswordLogin = findViewById(R.id.tiPasswordLogin);
            etEmailLogin = findViewById(R.id.etEmailLogin);
            etPasswordLogin = findViewById(R.id.etPasswordLogin);
            MaterialButton btnLogin = findViewById(R.id.btnLogin);

            tiNameSignup = findViewById(R.id.tiNameSignup);
            tiEmailSignup = findViewById(R.id.tiEmailSignup);
            tiPasswordSignup = findViewById(R.id.tiPasswordSignup);
            etNameSignup = findViewById(R.id.etNameSignup);
            etEmailSignup = findViewById(R.id.etEmailSignup);
            etPasswordSignup = findViewById(R.id.etPasswordSignup);

            MaterialButton btnSignup = findViewById(R.id.btnSignup);
            MaterialButton btnGoogle = findViewById(R.id.btnGoogle);

            MaterialCheckBox cbRememberMe = findViewById(R.id.cbRememberMe);
            MaterialCheckBox cbRememberMeSignUp = findViewById(R.id.cbRememberMeSignUp);
            AppCompatTextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

            fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

            typeFaceMedium = ResourcesCompat.getFont(getApplicationContext(), R.font.exo2_medium);
            typeFaceBold = ResourcesCompat.getFont(getApplicationContext(), R.font.exo2_bold);

            tabLogin.setOnClickListener(this);
            tabSignup.setOnClickListener(this);

            btnLogin.setOnClickListener(this);
            btnSignup.setOnClickListener(this);
            btnGoogle.setOnClickListener(this);

            mAuth = FirebaseAuth.getInstance();
            oneTapClient = Identity.getSignInClient(this);

            signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(getString(R.string.default_web_client_id))
                                    .setFilterByAuthorizedAccounts(false) // false = show all accounts
                                    .build())
                    .setAutoSelectEnabled(false) // false = always show One Tap prompt
                    .build();

            // Call our method to fetch token
            getFcmToken(new FcmTokenCallback() {
                @Override
                public void onTokenReceived(String token) {
                    if (token != null) {
                        AppLogger.d(getClass(), "Token received: " + token);
                        fcmToken = token;
                    } else {
                        AppLogger.w(getClass(), "Failed to get FCM token");
                    }
                }
            });

            loginViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            loginViewModel.getLoginStatus().observe(this, s -> {
                if (!s) {
                    showCustomDialog(loginViewModel.getErrorTitle(), loginViewModel.getErrorMessage(), false);
                }
            });

            loginViewModel.getProgressState().observe(this, isProgress -> {
                if (isProgress) {
                    showProgress(progressBar);
                } else {
                    hideProgress(progressBar);
                }
            });

            cbRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> isRememberMe = isChecked);

            cbRememberMeSignUp.setOnCheckedChangeListener((buttonView, isChecked) -> isRememberMe = isChecked);

            tvForgotPassword.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "Reset Clicked", LENGTH_SHORT).show());
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error initializing components", e);
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.tabLogin) {
                tabLogin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_selected_bg));
                tabLogin.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
                tabSignup.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_unselected_bg));
                tabSignup.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));

                tabLogin.setTypeface(typeFaceBold);
                tabSignup.setTypeface(typeFaceMedium);

                signupForm.startAnimation(fadeOut);
                signupForm.setVisibility(View.GONE);

                loginForm.setVisibility(View.VISIBLE);
                loginForm.startAnimation(fadeIn);

                isRememberMe = false;
            }

            if (v.getId() == R.id.tabSignup) {
                tabSignup.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_selected_bg));
                tabSignup.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
                tabLogin.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tab_unselected_bg));
                tabLogin.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));

                tabSignup.setTypeface(typeFaceBold);
                tabLogin.setTypeface(typeFaceMedium);

                loginForm.startAnimation(fadeOut);
                loginForm.setVisibility(View.GONE);

                signupForm.setVisibility(View.VISIBLE);
                signupForm.startAnimation(fadeIn);

                isRememberMe = false;
            }

            if (v.getId() == R.id.btnLogin) {
                validateFieldsLogin();
            }

            if (v.getId() == R.id.btnSignup) {
                validateFieldsSignup();
            }

            if (v.getId() == R.id.btnGoogle) {
                signInWithGoogle();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error onClick", e);
        }
    }

    private void validateFieldsLogin() {
        try {
            if (new NetworkConnectivity(this).isNetworkAvailable()) {
                boolean isValid1 = true, isValid2 = true;

                if (Objects.requireNonNull(etEmailLogin.getText()).toString().isEmpty()) {
                    tiEmailLogin.setError("Required Field");
                    tiEmailLogin.setErrorEnabled(true);
                    isValid1 = false;
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(etEmailLogin.getText()).toString()).matches()) {
                        tiEmailLogin.setError("Invalid email");
                        tiEmailLogin.setErrorEnabled(true);
                    } else {
                        tiEmailLogin.setError(null);
                        tiEmailLogin.setErrorEnabled(false);
                    }
                }

                if (Objects.requireNonNull(etPasswordLogin.getText()).toString().isEmpty()) {
                    tiPasswordLogin.setError("Required Field");
                    tiPasswordLogin.setErrorEnabled(true);
                    isValid2 = false;
                } else {
                    tiPasswordLogin.setError(null);
                    tiPasswordLogin.setErrorEnabled(false);
                }

                if (isValid1 && isValid2) {
                    LoginRequest loginRequest = new LoginRequest();
                    loginRequest.setEmail(etEmailLogin.getText().toString());
                    loginRequest.setPassword(etPasswordLogin.getText().toString());
                    loginRequest.setGoogleLogin(false);
                    loginRequest.setFcmToken(fcmToken);
                    loginRequest.setAppVersion(CommonUtils.getAppVersionName(this) + " " + CommonUtils.getAppVersionCode(this));
                    loginRequest.setDeviceId(CommonUtils.getDeviceId(getApplicationContext()));
                    loginRequest.setRememberMe(isRememberMe);

                    loginViewModel.login(loginRequest);

                    loginViewModel.getLoginResult().observe(this, loginResponse -> {

                        PreferenceManager.INSTANCE.setAccessToken(loginResponse.getAccessToken());
                        PreferenceManager.INSTANCE.setLoginExpiry(loginResponse.getLoginExpiresAt());
                        PreferenceManager.INSTANCE.setRefreshToken(loginResponse.getRefreshToken());
                        PreferenceManager.INSTANCE.setRefreshTokenExpiry(loginResponse.getRefreshExpiresAt());
                        PreferenceManager.INSTANCE.setName(loginResponse.getUser().getName());
                        PreferenceManager.INSTANCE.setEmail(loginResponse.getUser().getEmail());
                        PreferenceManager.INSTANCE.setLoggedIn(true);
                        PreferenceManager.INSTANCE.setGoogleSignIn(false);
                        PreferenceManager.INSTANCE.setSecretKey(loginResponse.getUser().getSecretKey());
                        PreferenceManager.INSTANCE.setRememberMe(isRememberMe);

                        startActivity(new Intent(this, MainActivity.class)
                                .putExtra("isFromLogin", true)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    });
                }
            } else {
                showCustomDialog(getString(R.string.information), getString(R.string.internet_not_available), false);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error validateFieldsLogin", e);
        }
    }

    private void validateFieldsSignup() {
        try {

            if (new NetworkConnectivity(this).isNetworkAvailable()) {


                boolean isValid1 = true, isValid2 = true, isValid3 = true;

                if (Objects.requireNonNull(etNameSignup.getText()).toString().isEmpty()) {
                    tiNameSignup.setError(getString(R.string.required_field));
                    tiNameSignup.setErrorEnabled(true);
                    isValid1 = false;
                } else {
                    tiNameSignup.setError(null);
                    tiNameSignup.setErrorEnabled(false);
                }

                if (Objects.requireNonNull(etEmailSignup.getText()).toString().isEmpty()) {
                    tiEmailSignup.setError(getString(R.string.required_field));
                    tiEmailSignup.setErrorEnabled(true);
                    isValid2 = false;
                } else {
                    if (!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(etEmailSignup.getText()).toString()).matches()) {
                        tiEmailSignup.setError("Invalid email");
                        tiEmailSignup.setErrorEnabled(true);
                    } else {
                        tiEmailSignup.setError(null);
                        tiEmailSignup.setErrorEnabled(false);
                    }
                }

                if (Objects.requireNonNull(etPasswordSignup.getText()).toString().isEmpty()) {
                    tiPasswordSignup.setError(getString(R.string.required_field));
                    tiPasswordSignup.setErrorEnabled(true);
                    isValid3 = false;
                } else {
                    tiPasswordSignup.setError(null);
                    tiPasswordSignup.setErrorEnabled(false);
                }

                if (isValid1 && isValid2 && isValid3) {
                    SignUpRequest signUpRequest = new SignUpRequest();
                    signUpRequest.setName(etNameSignup.getText().toString());
                    signUpRequest.setEmail(etEmailSignup.getText().toString());
                    signUpRequest.setPassword(etPasswordSignup.getText().toString());
                    signUpRequest.setGoogle(false);
                    signUpRequest.setFcmToken(fcmToken);
                    signUpRequest.setDeviceId(CommonUtils.getDeviceId(getApplicationContext()));
                    signUpRequest.setAppVersion(CommonUtils.getAppVersionName(this) + " " + CommonUtils.getAppVersionCode(this));
                    signUpRequest.setRememberMe(isRememberMe);

                    loginViewModel.register(signUpRequest);

                    loginViewModel.getLoginResult().observe(this, loginResponse -> {

                        PreferenceManager.INSTANCE.setAccessToken(loginResponse.getAccessToken());
                        PreferenceManager.INSTANCE.setLoginExpiry(loginResponse.getLoginExpiresAt());
                        PreferenceManager.INSTANCE.setRefreshToken(loginResponse.getRefreshToken());
                        PreferenceManager.INSTANCE.setRefreshTokenExpiry(loginResponse.getRefreshExpiresAt());
                        PreferenceManager.INSTANCE.setName(loginResponse.getUser().getName());
                        PreferenceManager.INSTANCE.setEmail(loginResponse.getUser().getEmail());
                        PreferenceManager.INSTANCE.setLoggedIn(true);
                        PreferenceManager.INSTANCE.setGoogleSignIn(false);
                        PreferenceManager.INSTANCE.setSecretKey(loginResponse.getUser().getSecretKey());
                        PreferenceManager.INSTANCE.setRememberMe(isRememberMe);

                        startActivity(new Intent(this, MainActivity.class)
                                .putExtra("isFromLogin", true)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    });
                }
            } else {
                showCustomDialog(getString(R.string.information), getString(R.string.internet_not_available), false);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error validateFieldsSignup", e);
        }
    }

    private void signInWithGoogle() {
        try {

            if (new NetworkConnectivity(this).isNetworkAvailable()) {
                showProgress(progressBar);
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(this, result -> {
                            try {
                                hideProgress(progressBar);
                                startIntentSenderForResult(
                                        result.getPendingIntent().getIntentSender(),
                                        REQ_ONE_TAP,
                                        null,
                                        0, 0, 0
                                );
                            } catch (Exception e) {
                                AppLogger.e(getClass(), "Error signInWithGoogle", e);
                                fallbackGoogleSignIn();
                            }
                        })
                        .addOnFailureListener(this, e -> {
                            AppLogger.e(getClass(), "One Tap failed", e);
                            fallbackGoogleSignIn();
                        });
            } else {
                showCustomDialog(getString(R.string.information), getString(R.string.internet_not_available), false);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error getting signInWithGoogle: ", e);
            fallbackGoogleSignIn();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken);
                }
            } catch (ApiException e) {
                AppLogger.e(getClass(), "Error onActivityResult", e);
                Toast.makeText(this, "One Tap sign-in failed: " + e.getMessage(), LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showProgress(progressBar);
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            SignUpRequest signUpRequest = new SignUpRequest();
                            signUpRequest.setName(user.getDisplayName());
                            signUpRequest.setEmail(user.getEmail());
                            signUpRequest.setPassword("");
                            signUpRequest.setGoogle(true);
                            signUpRequest.setFcmToken(fcmToken);
                            signUpRequest.setDeviceId(CommonUtils.getDeviceId(getApplicationContext()));
                            signUpRequest.setAppVersion(CommonUtils.getAppVersionName(this) + " " + CommonUtils.getAppVersionCode(this));
                            signUpRequest.setRememberMe(isRememberMe);

                            loginViewModel.register(signUpRequest);

                            loginViewModel.getLoginResult().observe(this, loginResponse -> {

                                PreferenceManager.INSTANCE.setAccessToken(loginResponse.getAccessToken());
                                PreferenceManager.INSTANCE.setLoginExpiry(loginResponse.getLoginExpiresAt());
                                PreferenceManager.INSTANCE.setRefreshToken(loginResponse.getRefreshToken());
                                PreferenceManager.INSTANCE.setRefreshTokenExpiry(loginResponse.getRefreshExpiresAt());
                                PreferenceManager.INSTANCE.setName(loginResponse.getUser().getName());
                                PreferenceManager.INSTANCE.setEmail(loginResponse.getUser().getEmail());
                                PreferenceManager.INSTANCE.setLoggedIn(true);
                                PreferenceManager.INSTANCE.setGoogleSignIn(true);
                                PreferenceManager.INSTANCE.setSecretKey(loginResponse.getUser().getSecretKey());
                                PreferenceManager.INSTANCE.setRememberMe(isRememberMe);

                                startActivity(new Intent(this, MainActivity.class)
                                        .putExtra("isFromLogin", true)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            });
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(), LENGTH_SHORT).show();
                    }
                });
    }

    private void fallbackGoogleSignIn() {
        try {
            hideProgress(progressBar);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQ_ONE_TAP);
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error fallbackGoogleSignIn", e);
        }
    }

    private void getFcmToken(FcmTokenCallback callback) {
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                AppLogger.e(getClass(), "Fetching FCM registration token failed", task.getException());
                                callback.onTokenReceived(null);
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();
                            AppLogger.d(getClass(), "FCM Token: " + token);
                            callback.onTokenReceived(token);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error while fetching FCM token", e);
            callback.onTokenReceived(null);
        }
    }

    public interface FcmTokenCallback {
        void onTokenReceived(String token);
    }
}