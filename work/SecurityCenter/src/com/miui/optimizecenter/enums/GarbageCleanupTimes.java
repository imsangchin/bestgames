
package com.miui.optimizecenter.enums;

import com.miui.securitycenter.R;
import android.content.res.Resources;

public enum GarbageCleanupTimes {

    DAILY(R.integer.pref_val_garbage_cleanup_daily),
    THREE_DAYS(R.integer.pref_val_garbage_cleanup_three_days),
    SEVEN_DAYS(R.integer.pref_val_garbage_cleanup_seven_days),
    NEVER(R.integer.pref_val_garbage_cleanup_never);

    private static final int DEFAULT = R.integer.pref_val_garbage_cleanup_three_days;
    private int mValueId;

    private GarbageCleanupTimes(int valueId) {
        mValueId = valueId;
    }

    public int getValue(Resources res) {
        return res.getInteger(mValueId);
    }

    public static GarbageCleanupTimes fromValue(Resources res, int value) {
        for (GarbageCleanupTimes b : GarbageCleanupTimes.values()) {
            if (value == b.getValue(res)) {
                return b;
            }
        }
        return getDefault(res);
    }

    public static GarbageCleanupTimes getDefault(Resources res) {
        for (GarbageCleanupTimes b : GarbageCleanupTimes.values()) {
            if (res.getInteger(DEFAULT) == b.getValue(res)) {
                return b;
            }
        }
        return null;
    }
}
