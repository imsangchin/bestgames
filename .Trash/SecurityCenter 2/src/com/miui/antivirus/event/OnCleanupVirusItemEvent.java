
package com.miui.antivirus.event;

public class OnCleanupVirusItemEvent {
    private String mDescx;

    private OnCleanupVirusItemEvent() {
        // ignore
    }

    public static OnCleanupVirusItemEvent create(String descx) {
        OnCleanupVirusItemEvent res = new OnCleanupVirusItemEvent();
        res.mDescx = descx;
        return res;
    }

    public String getDescx() {
        return mDescx;
    }
}
