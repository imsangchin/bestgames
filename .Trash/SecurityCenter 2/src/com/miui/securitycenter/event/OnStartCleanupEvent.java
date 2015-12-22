
package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItem;

public class OnStartCleanupEvent {

    private HandleItem mHandleItem;

    private OnStartCleanupEvent() {
        // ingore
    }

    public static OnStartCleanupEvent create(HandleItem handleItem) {
        OnStartCleanupEvent res = new OnStartCleanupEvent();
        res.mHandleItem = handleItem;
        return res;
    }

    public HandleItem getHandleItem() {
        return mHandleItem;
    }

}
