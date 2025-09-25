package com.nprotech.passwordmanager.view.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private AppCompatTextView tabLogin, tabSignup;
    private LinearLayout loginForm, signupForm;
    private TextInputLayout tiEmailLogin, tiPasswordLogin, tiNameSignup, tiEmailSignup, tiPasswordSignup;
    private TextInputEditText etEmailLogin, etPasswordLogin, etNameSignup, etEmailSignup, etPasswordSignup;
    private Animation fadeIn, fadeOut;
    private Typeface typeFaceMedium, typeFaceBold;
    private static final int REQ_ONE_TAP = 123;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseAuth mAuth;

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

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
            // Configure request
            signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(getString(R.string.default_web_client_id)) // from google-services.json
                                    .setFilterByAuthorizedAccounts(false)
                                    .build())
                    .setAutoSelectEnabled(false) // always ask user to choose
                    .build();

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
            if (Objects.requireNonNull(etEmailLogin.getText()).toString().isEmpty()) {
                tiEmailLogin.setError("Required Field");
                tiEmailLogin.setErrorEnabled(true);
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
            } else {
                tiPasswordLogin.setError(null);
                tiPasswordLogin.setErrorEnabled(false);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error validateFieldsLogin", e);
        }
    }

    private void validateFieldsSignup() {
        try {

            if (Objects.requireNonNull(etNameSignup.getText()).toString().isEmpty()) {
                tiNameSignup.setError("Required Field");
                tiNameSignup.setErrorEnabled(true);
            } else {
                tiNameSignup.setError(null);
                tiNameSignup.setErrorEnabled(false);
            }

            if (Objects.requireNonNull(etEmailSignup.getText()).toString().isEmpty()) {
                tiEmailSignup.setError("Required Field");
                tiEmailSignup.setErrorEnabled(true);
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
                tiPasswordSignup.setError("Required Field");
                tiPasswordSignup.setErrorEnabled(true);
            } else {
                tiPasswordSignup.setError(null);
                tiPasswordSignup.setErrorEnabled(false);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error validateFieldsSignup", e);
        }
    }

    private void signInWithGoogle() {
        try {
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this, new OnSuccessListener<>() {
                        @Override
                        public void onSuccess(BeginSignInResult result) {
                            try {
                                startIntentSenderForResult(
                                        result.getPendingIntent().getIntentSender(),
                                        REQ_ONE_TAP,
                                        null,
                                        0,
                                        0,
                                        0
                                );
                            } catch (Exception e) {
                                AppLogger.e(getClass(), "Error starting One Tap: ", e);
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AppLogger.e(getClass(), "No saved credentials, use manual sign-in: ", e);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "Error getting signInWithGoogle: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        Toast.makeText(this, "Signed in as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    AppLogger.e(getClass(), "Sign-in failed", task.getException());
                                }
                            });
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "Failed to get credential: ", e);
            }
        }
    }
}