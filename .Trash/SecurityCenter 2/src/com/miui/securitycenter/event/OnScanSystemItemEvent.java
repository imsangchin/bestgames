package com.miui.securitycenter.event;

public class OnScanSystemItemEvent {
    private String mDescx;

    private OnScanSystemItemEvent() {
        // ingore
    }

    public static OnScanSystemItemEvent create(String descx) {
        OnScanSystemItemEvent res = new OnScanSystemItemEvent();
        res.mDescx = descx;
        return res;
    }

    public String getDescx() {
        return mDescx;
    }

}
