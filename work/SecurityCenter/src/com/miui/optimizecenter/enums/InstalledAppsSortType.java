
package com.miui.optimizecenter.enums;

import com.miui.securitycenter.R;
import android.content.res.Resources;

public enum InstalledAppsSortType {

    LUNCH_TIME(R.integer.pref_val_installed_apps_sort_by_lunch_time),
    APP_SIZE(R.integer.pref_val_installed_apps_sort_by_pkg_size);

    private static final int DEFAULT = R.integer.pref_val_installed_apps_sort_by_lunch_time;
    private int mValueId;

    private InstalledAppsSortType(int valueId) {
        mValueId = valueId;
    }

    public int getValue(Resources res) {
        return res.getInteger(mValueId);
    }

    public static InstalledAppsSortType fromValue(Resources res, int value) {
        for (InstalledAppsSortType b : InstalledAppsSortType.values()) {
            if (value == b.getValue(res)) {
                return b;
            }
        }
        return getDefault(res);
    }

    public static InstalledAppsSortType getDefault(Resources res) {
        for (InstalledAppsSortType b : InstalledAppsSortType.values()) {
            if (res.getInteger(DEFAULT) == b.getValue(res)) {
                return b;
            }
        }
        return null;
    }
}
