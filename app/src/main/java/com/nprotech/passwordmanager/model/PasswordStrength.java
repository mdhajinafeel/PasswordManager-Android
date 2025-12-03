package com.nprotech.passwordmanager.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public class PasswordStrength implements Serializable {

    private final int strengthType, color, backgroundResNormal, backgroundResDark;
    private final String strengthLabel;

    public PasswordStrength(int type, int color, String label,
                            int bgNormal, int bgDark) {
        this.strengthType = type;
        this.color = color;
        this.strengthLabel = label;
        this.backgroundResNormal = bgNormal;
        this.backgroundResDark = bgDark;
    }

    public int getStrengthType() { return strengthType; }
    public int getColor() { return color; }
    public String getStrengthLabel() { return strengthLabel; }
    public int getBackgroundResNormal() { return backgroundResNormal; }
    public int getBackgroundResDark() { return backgroundResDark; }
}