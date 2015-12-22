
package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItem;

public class OnFinishCleanupEvent {

    private HandleItem mHandleItem;

    private OnFinishCleanupEvent() {
        // ingore
    }

    public static OnFinishCleanupEvent create(HandleItem handleItem) {
        OnFinishCleanupEvent res = new OnFinishCleanupEvent();
        res.mHandleItem = handleItem;
        return res;
    }

    public HandleItem getHandleItem() {
        return mHandleItem;
    }

}
