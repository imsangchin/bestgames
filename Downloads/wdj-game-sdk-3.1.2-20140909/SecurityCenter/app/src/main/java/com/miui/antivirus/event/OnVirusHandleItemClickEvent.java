
package com.miui.antivirus.event;

import com.miui.antivirus.MainHandleBar.HandleItem;

public class OnVirusHandleItemClickEvent {
    private HandleItem mHandleItem;

    private OnVirusHandleItemClickEvent() {
        // ignore
    }

    public static OnVirusHandleItemClickEvent create(HandleItem item) {
        OnVirusHandleItemClickEvent res = new OnVirusHandleItemClickEvent();
        res.mHandleItem = item;
        return res;
    }

    public HandleItem getHandleItem() {
        return mHandleItem;
    }
}
