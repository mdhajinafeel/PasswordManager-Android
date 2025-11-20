package com.nprotech.passwordmanager.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public abstract class CommonRecyclerViewAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private final Context context;
    private final int layoutId;
    private List<T> dataList;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public CommonRecyclerViewAdapter(@NonNull Context context, List<T> dataList, int layoutId) {
        this.context = context;
        this.layoutId = layoutId;
        this.dataList = dataList != null ? new ArrayList<>(dataList) : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        T item = dataList.get(position);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, holder.getBindingAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v ->
                onItemLongClickListener != null &&
                        onItemLongClickListener.onItemLongClick(v, holder.getBindingAdapterPosition())
        );

        onPostBindViewHolder(holder, item);
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public abstract void onPostBindViewHolder(@NonNull ViewHolder holder, @NonNull T item);

    public T getItem(int position) {
        return (dataList != null && position >= 0 && position < dataList.size()) ? dataList.get(position) : null;
    }

    public void add(@NonNull T item) {
        dataList.add(item);
        notifyItemInserted(dataList.size() - 1);
    }

    public void addAll(@NonNull List<T> items) {
        int startPos = dataList.size();
        dataList.addAll(items);
        notifyItemRangeInserted(startPos, items.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<T> newData) {
        dataList.clear();
        dataList.addAll(newData);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(@NonNull List<T> filteredList) {
        dataList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(@NonNull List<T> newData) {
        this.dataList = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    public List<T> getDataList() {
        return Collections.unmodifiableList(dataList);
    }

    public void addItem(@NonNull T item) {
        this.dataList.add(item);
    }

    public void addItemAtTop(T item) {
        if (dataList != null) {
            dataList.add(0, item);
        }
    }

    // Listener Interfaces
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}