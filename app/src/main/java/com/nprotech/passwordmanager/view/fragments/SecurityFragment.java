package com.nprotech.passwordmanager.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.utils.AppLogger;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SecurityFragment extends Fragment {

    public static SecurityFragment getInstance() {
        return new SecurityFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security, container, false);
        try {
            AppCompatTextView txtTitle = view.findViewById(R.id.txtTitle);

            txtTitle.setText(getString(R.string.security));
        } catch (Exception e) {
            AppLogger.e(getInstance().getClass(), "Error onCreateView", e);
        }

        return view;
    }
}