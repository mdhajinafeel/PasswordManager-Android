package com.nprotech.passwordmanager.view.adapter;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public class ViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;

    public ViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }
    public ViewHolder(View itemView, int forPagination) {
        super(itemView);
    }
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);

        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }

        //noinspection unchecked
        return (T) view;
    }

    public void setViewText(int viewId, CharSequence text) {
        ReflectHelper.invokeMethodIfExists("setText", getView(viewId), new Class[]{CharSequence.class}, text);
    }

    public ViewHolder setViewText(int viewId, int resId) {
        ReflectHelper.invokeMethodIfExists("setText", getView(viewId), new Class[]{int.class}, resId);
        return this;
    }

    public void setViewTextColor(int viewId, int color) {
        ReflectHelper.invokeMethodIfExists("setTextColor", getView(viewId), new Class[]{int.class}, color);
    }

    public ViewHolder setViewTextSize(int viewId, float size) {
        ReflectHelper.invokeMethodIfExists("setTextSize", getView(viewId), new Class[]{float.class}, size);
        return this;
    }

    public ViewHolder setViewTextSize(int viewId, int unit, float size) {
        ReflectHelper.invokeMethodIfExists("setTextSize", getView(viewId), new Class[]{int.class, float.class}, unit, size);
        return this;
    }

    public ViewHolder setViewImageResource(int viewId, int resId) {
        ReflectHelper.invokeMethodIfExists("setImageResource", getView(viewId), new Class[]{int.class}, resId);
        return this;
    }

    public void setViewImageBitmap(int viewId, Bitmap bitmap) {
        ReflectHelper.invokeMethodIfExists("setImageBitmap", getView(viewId), new Class[]{Bitmap.class}, bitmap);
    }

    public ViewHolder setViewImageDrawable(int viewId, Drawable drawable) {
        ReflectHelper.invokeMethodIfExists("setImageDrawable", getView(viewId), new Class[]{Drawable.class}, drawable);
        return this;
    }

    public ViewHolder setViewImageURI(int viewId, Uri uri) {
        ReflectHelper.invokeMethodIfExists("setImageURI", getView(viewId), new Class[]{Uri.class}, uri);
        return this;
    }

    public ViewHolder setViewChecked(int viewId, boolean checked) {
        ReflectHelper.invokeMethodIfExists("setChecked", getView(viewId), new Class[]{boolean.class}, checked);
        return this;
    }

    public ViewHolder setViewOnClickListener(int viewId, View.OnClickListener listener) {
        ReflectHelper.invokeMethodIfExists("setOnClickListener", getView(viewId), new Class[]{View.OnClickListener.class}, listener);
        return this;
    }

    public ViewHolder setViewOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        ReflectHelper.invokeMethodIfExists("setOnLongClickListener", getView(viewId), new Class[]{View.OnLongClickListener.class}, listener);
        return this;
    }

    public ViewHolder setViewVisibility(int viewId, int visibility) {
        ReflectHelper.invokeMethodIfExists("setVisibility", getView(viewId), new Class[]{int.class}, visibility);
        return this;
    }

    public ViewHolder setViewTypeface(int viewId, Typeface typeface){
        ReflectHelper.invokeMethodIfExists("setTypeface", getView(viewId), new Class[]{Typeface.class}, typeface);
        return this;
    }

    public ViewHolder setViewPaintFlags(int viewId, int flags){
        ReflectHelper.invokeMethodIfExists("setPaintFlags", getView(viewId), new Class[]{int.class}, flags);
        return this;
    }

    public ViewHolder setViewProperty(int viewId, String propertyName, Object... params) {
        propertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        ReflectHelper.invokeMethodIfExists("set" + propertyName, getView(viewId), params);
        return this;
    }

  public ViewHolder setSelected(int viewId, boolean checked) {
        ReflectHelper.invokeMethodIfExists("setSelected", getView(viewId), new Class[]{boolean.class}, checked); 
        return this;
    }
}
