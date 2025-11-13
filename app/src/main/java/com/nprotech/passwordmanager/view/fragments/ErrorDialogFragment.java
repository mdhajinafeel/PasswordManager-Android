package com.nprotech.passwordmanager.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nprotech.passwordmanager.R;

public class ErrorDialogFragment extends DialogFragment {

    private static final String ARG_HEADER = "header";
    private static final String ARG_BODY = "body";
    private static final String ARG_CANCEL = "cancel";

    public static ErrorDialogFragment newInstance(String header, String body, boolean isCancel) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HEADER, header);
        args.putString(ARG_BODY, body);
        args.putBoolean(ARG_CANCEL, isCancel);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String header = getArguments() != null ? getArguments().getString(ARG_HEADER) : "";
        String body = getArguments() != null ? getArguments().getString(ARG_BODY) : "";
        boolean isCancel = getArguments() != null && getArguments().getBoolean(ARG_CANCEL);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_dialog, null);

        AppCompatTextView tvHeader = view.findViewById(R.id.dialogHeader);
        AppCompatTextView tvBody = view.findViewById(R.id.dialogBody);
        MaterialButton btnOk = view.findViewById(R.id.btnOk);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        tvHeader.setText(header);
        tvBody.setText(body);
        btnCancel.setVisibility(isCancel ? View.VISIBLE : View.GONE);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnOk.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }
}