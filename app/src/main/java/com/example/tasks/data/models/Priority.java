package com.example.tasks.data.models;

import com.example.tasks.R;

/**
 * 优先级枚举
 */
public enum Priority {
    LOW("低", R.color.priority_low),
    MEDIUM("中", R.color.priority_medium),
    HIGH("高", R.color.priority_high),
    URGENT("紧急", R.color.priority_urgent);

    private final String displayName;
    private final int colorRes;

    Priority(String displayName, int colorRes) {
        this.displayName = displayName;
        this.colorRes = colorRes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorRes() {
        return colorRes;
    }
}