
package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItem;

public class OnStartScanEvent {

    private HandleItem item;
    private OnStartScanEvent() {
        // ingore
    }

    public static OnStartScanEvent create(HandleItem item) {
        OnStartScanEvent res = new OnStartScanEvent();
        res.item = item;
        return res;
    }

    public HandleItem getItem() {
        return item;
    }
}
