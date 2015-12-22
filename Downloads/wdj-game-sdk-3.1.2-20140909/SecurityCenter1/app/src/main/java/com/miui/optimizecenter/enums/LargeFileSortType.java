
package com.miui.optimizecenter.enums;

import com.miui.securitycenter.R;
import android.content.res.Resources;

public enum LargeFileSortType {

    SIZE(R.integer.pref_val_large_file_sort_by_size),
    NAME(R.integer.pref_val_large_file_sort_by_name);

    private static final int DEFAULT = R.integer.pref_val_large_file_sort_by_size;
    private int mValueId;

    private LargeFileSortType(int valueId) {
        mValueId = valueId;
    }

    public int getValue(Resources res) {
        return res.getInteger(mValueId);
    }

    public static LargeFileSortType fromValue(Resources res, int value) {
        for (LargeFileSortType b : LargeFileSortType.values()) {
            if (value == b.getValue(res)) {
                return b;
            }
        }
        return getDefault(res);
    }

    public static LargeFileSortType getDefault(Resources res) {
        for (LargeFileSortType b : LargeFileSortType.values()) {
            if (res.getInteger(DEFAULT) == b.getValue(res)) {
                return b;
            }
        }
        return null;
    }
}
