package com.nprotech.passwordmanager.view.activities;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.common.BaseActivity;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.utils.AppLogger;
import com.nprotech.passwordmanager.view.fragments.CategoryFragment;
import com.nprotech.passwordmanager.view.fragments.ErrorDialogFragment;
import com.nprotech.passwordmanager.view.fragments.FavoritesFragment;
import com.nprotech.passwordmanager.view.fragments.HomeFragment;
import com.nprotech.passwordmanager.view.fragments.SettingsFragment;
import com.nprotech.passwordmanager.viewmodel.MasterViewModel;
import com.nprotech.passwordmanager.work.SyncScheduler;
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    private FrameLayout progressBar;
    private MasterViewModel masterViewModel;
    private boolean isFragmentAlreadyLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Show a dialog directing the user to settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                }
            }

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

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                if (bundle.getBoolean("isFromLogin")) {
                    PreferenceManager.INSTANCE.setSyncId(1);
                    PreferenceManager.INSTANCE.setSyncHours(1);

                    SyncScheduler.scheduleHourlySync(getApplicationContext(), PreferenceManager.INSTANCE.getSyncHours());
                }
            }

            CurvedBottomNavigation curvedBottomNavigation = findViewById(R.id.curvedBottomNavigation);

            // Add Menu Items Programmatically
            curvedBottomNavigation.add(new CurvedBottomNavigation.Model(1, getString(R.string.home), R.drawable.ic_home));
            curvedBottomNavigation.add(new CurvedBottomNavigation.Model(2, getString(R.string.categories), R.drawable.ic_category_menu));
            curvedBottomNavigation.add(new CurvedBottomNavigation.Model(3, getString(R.string.favourites), R.drawable.ic_favorites));
            curvedBottomNavigation.add(new CurvedBottomNavigation.Model(4, getString(R.string.settings), R.drawable.ic_settings));

            // Set default selected item
            curvedBottomNavigation.setOnShowListener(item -> {

                if (isFragmentAlreadyLoaded) {
                    return null;   // ⛔ prevent fragment reload when keyboard opens
                }

                isFragmentAlreadyLoaded = true;

                Fragment selectedFragment = null;

                switch (item.getId()) {
                    case 1:
                        selectedFragment = HomeFragment.getInstance();
                        break;
                    case 2:
                        selectedFragment = CategoryFragment.getInstance();
                        break;
                    case 3:
                        selectedFragment = FavoritesFragment.getInstance();
                        break;
                    case 4:
                        selectedFragment = SettingsFragment.getInstance();
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return null;
            });

            curvedBottomNavigation.setOnClickMenuListener(model -> {
                isFragmentAlreadyLoaded = false;
                return null;
            });
            curvedBottomNavigation.setOnReselectListener(model -> null);

            // Show the first item by default
            curvedBottomNavigation.show(1, true);

            progressBar = findViewById(R.id.progressBar);

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

            masterViewModel.getErrorMessage().observe(this, s -> ErrorDialogFragment.newInstance(
                    masterViewModel.getErrorTitle().getValue(),
                    masterViewModel.getErrorMessage().getValue(),
                    false
            ).show(getSupportFragmentManager(), "errorDialog"));

            masterViewModel.getProgressState().observe(this, s -> {
                if (s) {
                    showProgress();
                } else {
                    hideProgress();
                }
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "Error initComponents", e);
        }
    }

    public void showProgress() {
        showProgress(progressBar);
    }

    public void hideProgress() {
        hideProgress(progressBar);
    }
}