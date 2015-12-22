
package com.miui.securitycenter.event;

public class OnCleanupItemEvent {
    private String mDescx;

    private OnCleanupItemEvent() {
        // ingore
    }

    public static OnCleanupItemEvent create(String descx) {
        OnCleanupItemEvent res = new OnCleanupItemEvent();
        res.mDescx = descx;
        return res;
    }

    public String getDescx() {
        return mDescx;
    }

}
