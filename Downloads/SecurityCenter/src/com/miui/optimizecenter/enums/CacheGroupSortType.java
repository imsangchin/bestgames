
package com.miui.optimizecenter.enums;

import com.miui.securitycenter.R;
import android.content.res.Resources;

public enum CacheGroupSortType {

    SIZE(R.integer.pref_val_cache_data_sort_by_size),
    NAME(R.integer.pref_val_cache_data_sort_by_name);

    private static final int DEFAULT = R.integer.pref_val_cache_data_sort_by_size;
    private int mValueId;

    private CacheGroupSortType(int valueId) {
        mValueId = valueId;
    }

    public int getValue(Resources res) {
        return res.getInteger(mValueId);
    }

    public static CacheGroupSortType fromValue(Resources res, int value) {
        for (CacheGroupSortType b : CacheGroupSortType.values()) {
            if (value == b.getValue(res)) {
                return b;
            }
        }
        return getDefault(res);
    }

    public static CacheGroupSortType getDefault(Resources res) {
        for (CacheGroupSortType b : CacheGroupSortType.values()) {
            if (res.getInteger(DEFAULT) == b.getValue(res)) {
                return b;
            }
        }
        return null;
    }
}
