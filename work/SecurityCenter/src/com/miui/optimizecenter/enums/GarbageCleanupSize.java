
package com.miui.optimizecenter.enums;

import com.miui.securitycenter.R;
import android.content.res.Resources;

public enum GarbageCleanupSize {

    M100(R.integer.pref_val_garbage_size_100m),
    M300(R.integer.pref_val_garbage_size_300m),
    M500(R.integer.pref_val_garbage_size_500m),
    M1000(R.integer.pref_val_garbage_size_1000m);

    private static final int DEFAULT = R.integer.pref_val_garbage_size_100m;
    private int mValueId;

    private GarbageCleanupSize(int valueId) {
        mValueId = valueId;
    }

    public int getValue(Resources res) {
        return res.getInteger(mValueId);
    }

    public static GarbageCleanupSize fromValue(Resources res, int value) {
        for (GarbageCleanupSize b : GarbageCleanupSize.values()) {
            if (value == b.getValue(res)) {
                return b;
            }
        }
        return getDefault(res);
    }

    public static GarbageCleanupSize getDefault(Resources res) {
        for (GarbageCleanupSize b : GarbageCleanupSize.values()) {
            if (res.getInteger(DEFAULT) == b.getValue(res)) {
                return b;
            }
        }
        return null;
    }
}
