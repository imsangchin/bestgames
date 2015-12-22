
package com.miui.antivirus.event;

public class OnScanVirusItemEvent {

    private String mDescx;

    private OnScanVirusItemEvent() {
        // ignore
    }

    public static OnScanVirusItemEvent create(String descx) {
        OnScanVirusItemEvent res = new OnScanVirusItemEvent();
        res.mDescx = descx;
        return res;
    }

    public String getDescx() {
        return mDescx;
    }
}
