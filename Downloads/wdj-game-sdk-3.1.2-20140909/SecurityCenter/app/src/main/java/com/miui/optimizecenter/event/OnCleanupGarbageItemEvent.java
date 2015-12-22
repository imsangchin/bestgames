
package com.miui.optimizecenter.event;

public class OnCleanupGarbageItemEvent {
    private String mDescx;

    private OnCleanupGarbageItemEvent() {
        // ignore
    }

    public static OnCleanupGarbageItemEvent create(String descx) {
        OnCleanupGarbageItemEvent res = new OnCleanupGarbageItemEvent();
        res.mDescx = descx;
        return res;
    }

    public String getDescx() {
        return mDescx;
    }
}
