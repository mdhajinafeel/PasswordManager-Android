package com.nprotech.passwordmanager.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.toggle.widget.LabeledSwitch;
import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.helper.PreferenceManager;
import com.nprotech.passwordmanager.model.SettingItem;
import com.nprotech.passwordmanager.model.SyncInterval;

import java.util.ArrayList;
import java.util.List;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    public interface OnSettingActionListener {
        void onSettingClick(SettingItem item, int itemId, int itemValue);

        void onSwitchToggle(SettingItem item, boolean isChecked, LabeledSwitch switchButton);
    }

    private final List<SettingItem> items;
    private final Context context;
    private final OnSettingActionListener listener;

    private final List<SyncInterval> intervals;
    private final SyncIntervalAdapter syncAdapter;

    public SettingsRecyclerAdapter(Context context, List<SettingItem> items, OnSettingActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;

        intervals = new ArrayList<>();
        intervals.add(new SyncInterval(1, context.getString(R.string.sync_interval_1), 1));
        intervals.add(new SyncInterval(2, context.getString(R.string.sync_interval_2), 3));
        intervals.add(new SyncInterval(3, context.getString(R.string.sync_interval_3), 6));
        intervals.add(new SyncInterval(4, context.getString(R.string.sync_interval_4), 12));
        intervals.add(new SyncInterval(5, context.getString(R.string.sync_interval_5), 24));
        intervals.add(new SyncInterval(6, context.getString(R.string.sync_interval_manual), 0));

        syncAdapter = new SyncIntervalAdapter(context, intervals);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItem item = items.get(position);
        holder.icon.setImageResource(item.getIconRes());
        holder.title.setText(item.getTitle());

        if (item.isSwitchVisible()) {
            holder.switchButton.setVisibility(View.VISIBLE);
            holder.spinnerContainer.setVisibility(View.GONE);
            holder.downloadContainer.setVisibility(View.GONE);

            holder.switchButton.setOnToggledListener(null);
            if (item.getSettingId() == 1)
                holder.switchButton.setOn(PreferenceManager.INSTANCE.getBioMetric());
            else if (item.getSettingId() == 2)
                holder.switchButton.setOn(PreferenceManager.INSTANCE.getDarkMode());

            holder.switchButton.setOnToggledListener((buttonView, isChecked) -> {
                item.setEnabled(isChecked);
                if (listener != null) listener.onSwitchToggle(item, isChecked, holder.switchButton);
            });

        } else if (item.getSettingId() == 3) {
            holder.switchButton.setVisibility(View.GONE);
            holder.spinnerContainer.setVisibility(View.GONE);
            holder.downloadContainer.setVisibility(View.VISIBLE);

            holder.ivExcel.setOnClickListener(v -> {
                Toast.makeText(context, "Excel Clicked1", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Excel Clicked", Toast.LENGTH_SHORT).show();
            });

            holder.ivPdf.setOnClickListener(v -> {
                Toast.makeText(context, "Pdf Clicked1", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Pdf Clicked", Toast.LENGTH_SHORT).show();
            });

        } else if (item.getSettingId() == 4) {
            holder.switchButton.setVisibility(View.GONE);
            holder.spinnerContainer.setVisibility(View.VISIBLE);
            holder.downloadContainer.setVisibility(View.GONE);

            holder.spinner.setAdapter(syncAdapter);

            final boolean[] isUserSelection = {false};
            holder.spinner.setOnTouchListener((v, event) -> {
                isUserSelection[0] = true;
                return false;
            });

            holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    if (!isUserSelection[0]) return;
                    SyncInterval selected = intervals.get(pos);
                    item.setSelectedValue(selected.getLabel());
                    if (listener != null)
                        listener.onSettingClick(item, selected.getId(), selected.getHours());
                    isUserSelection[0] = false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            int savedId = PreferenceManager.INSTANCE.getSyncId();
            if (savedId > 0 && savedId - 1 < intervals.size()) {
                holder.spinner.setSelection(savedId - 1, false);
            }

        } else {
            holder.switchButton.setVisibility(View.GONE);
            holder.spinnerContainer.setVisibility(View.GONE);
            holder.downloadContainer.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSettingClick(item, 0, 0);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ✅ Make ViewHolder public static
    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon, ivExcel, ivPdf;
        AppCompatTextView title;
        LabeledSwitch switchButton;
        View spinnerContainer, downloadContainer;
        AppCompatSpinner spinner;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            switchButton = itemView.findViewById(R.id.toggleSwitch);
            spinnerContainer = itemView.findViewById(R.id.spinnerContainer);
            downloadContainer = itemView.findViewById(R.id.downloadContainer);
            ivExcel = itemView.findViewById(R.id.ivExcel);
            ivPdf = itemView.findViewById(R.id.ivPdf);
            spinner = itemView.findViewById(R.id.syncInterval);
        }
    }
}