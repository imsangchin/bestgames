
package com.miui.securitycenter.event;

import java.util.List;

public class OnScanItemEvent {

    String mScanText;
    int mCount;
    int mTotalCount;

    private OnScanItemEvent() {
        // ingore
    }

    public static OnScanItemEvent create(String scanText, int count, int totalCount) {
        OnScanItemEvent res = new OnScanItemEvent();
        res.mScanText = scanText;
        res.mCount = count;
        res.mTotalCount = totalCount;
        return res;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public String getScanText() {
        return mScanText;
    }

    public int getCount() {
        return mCount;
    }
}
