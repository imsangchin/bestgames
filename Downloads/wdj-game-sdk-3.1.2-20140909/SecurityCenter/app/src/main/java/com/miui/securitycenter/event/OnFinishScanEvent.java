
package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItem;

import java.util.List;

public class OnFinishScanEvent {

    private HandleItem mItem;

    private OnFinishScanEvent() {
        // ingore
    }

    public static OnFinishScanEvent create(HandleItem item) {
        OnFinishScanEvent res = new OnFinishScanEvent();
        res.mItem = item;
        return res;
    }

    public HandleItem getHandleItem() {
        return mItem;
    }
}
