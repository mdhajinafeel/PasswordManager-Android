package com.nprotech.passwordmanager.model;

@SuppressWarnings("unused")
public class SettingItem {

    private final int iconRes, settingId;
    private final String title;
    private final boolean switchVisible;
    private boolean isEnabled;
    private boolean hasSpinner;
    private String selectedValue;

    public int getSettingId() {
        return settingId;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public boolean isSwitchVisible() {
        return switchVisible;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean hasSpinner() {
        return hasSpinner;
    }

    public void setHasSpinner(boolean hasSpinner) {
        this.hasSpinner = hasSpinner;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String value) {
        this.selectedValue = value;
    }

    public SettingItem(int settingId, int iconRes, String title, boolean switchVisible, boolean isEnabled, boolean hasSpinner) {
        this.settingId = settingId;
        this.iconRes = iconRes;
        this.title = title;
        this.switchVisible = switchVisible;
        this.isEnabled = isEnabled;
        this.hasSpinner = hasSpinner;
    }
}