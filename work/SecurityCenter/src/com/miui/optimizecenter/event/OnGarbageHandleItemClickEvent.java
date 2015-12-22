
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.MainHandleBar.HandleItem;

public class OnGarbageHandleItemClickEvent {

    private HandleItem mHandleItem;

    private OnGarbageHandleItemClickEvent() {
        // ignore
    }

    public static OnGarbageHandleItemClickEvent create(HandleItem item) {
        OnGarbageHandleItemClickEvent res = new OnGarbageHandleItemClickEvent();
        res.mHandleItem = item;
        return res;
    }

    public HandleItem getHandleItem() {
        return mHandleItem;
    }
}
