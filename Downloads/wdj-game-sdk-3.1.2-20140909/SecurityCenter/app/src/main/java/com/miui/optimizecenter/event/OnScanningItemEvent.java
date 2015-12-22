
package com.miui.optimizecenter.event;

public class OnScanningItemEvent {
    private String mDescx;

    private OnScanningItemEvent() {
        // ignore
    }

    public static OnScanningItemEvent create(String descx) {
        OnScanningItemEvent res = new OnScanningItemEvent();
        res.mDescx = descx;
        return res;
    }

    public String getDescx() {
        return mDescx;
    }
}
