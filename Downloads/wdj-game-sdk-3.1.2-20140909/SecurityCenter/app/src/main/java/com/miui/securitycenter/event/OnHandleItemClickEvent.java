
package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItem;

public class OnHandleItemClickEvent {

    private OnHandleItemClickEvent() {
        // ingore
    }

    private HandleItem mHandleItem;

    public static OnHandleItemClickEvent create(HandleItem item) {
        OnHandleItemClickEvent res = new OnHandleItemClickEvent();
        res.mHandleItem = item;
        return res;
    }

    public HandleItem getHandleItem() {
        return mHandleItem;
    }

}
