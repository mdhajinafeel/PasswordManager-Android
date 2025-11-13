package com.nprotech.passwordmanager.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.model.SyncInterval;

import java.util.List;

public class SyncIntervalAdapter extends ArrayAdapter<SyncInterval> {
    private final LayoutInflater inflater;

    public SyncIntervalAdapter(@NonNull Context context, @NonNull List<SyncInterval> intervals) {
        super(context, R.layout.spinner_list_item, intervals);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent, R.layout.spinner_list_item);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent, R.layout.spinner_dropdown_item);
    }

    private View createCustomView(int position, View convertView, ViewGroup parent, int layoutRes) {
        if (convertView == null) {
            convertView = inflater.inflate(layoutRes, parent, false);
        }

        AppCompatTextView textView = convertView.findViewById(R.id.tvSpinnerItem); // ✅ must exist
        SyncInterval syncInterval = getItem(position);

        if (syncInterval != null) {
            textView.setText(syncInterval.getLabel());
        }

        return convertView;
    }
}