package com.nprotech.passwordmanager.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.nprotech.passwordmanager.R;
import com.nprotech.passwordmanager.db.entities.CategoryEntity;
import com.nprotech.passwordmanager.utils.AppLogger;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<CategoryEntity> {

    private final LayoutInflater inflater;

    public CategoryAdapter(@NonNull Context context, @NonNull List<CategoryEntity> categories) {
        super(context, 0, categories);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_category_dropdown, parent, false);
        }

        AppCompatImageView imgIcon = view.findViewById(R.id.imgIcon);
        AppCompatTextView tvName = view.findViewById(R.id.tvCategoryName);

        CategoryEntity category = getItem(position);
        if (category != null) {

            // 🔹 Decode Base64 -> Bitmap
            String base64Icon = category.getIconText();
            if (base64Icon != null && !base64Icon.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(base64Icon, Base64.DEFAULT);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // scale down if needed
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
                    imgIcon.setImageBitmap(bitmap);
                } catch (Exception e) {
                    AppLogger.e(getClass(), "createView", e);
                    // fallback image if decoding fails
                    imgIcon.setImageResource(R.drawable.ic_default_category);
                }
            } else {
                imgIcon.setImageResource(R.drawable.ic_default_category);
            }

            tvName.setText(category.getCategoryName());
        }

        return view;
    }
}