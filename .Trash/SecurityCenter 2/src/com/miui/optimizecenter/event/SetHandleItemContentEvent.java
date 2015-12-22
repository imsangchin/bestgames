
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.MainHandleBar.HandleItem;

public class SetHandleItemContentEvent {
    private HandleItem mHandleItem;
    private CharSequence mContent;

    private SetHandleItemContentEvent() {
        // ignore
    }

    public static SetHandleItemContentEvent create(HandleItem item, CharSequence content) {
        SetHandleItemContentEvent res = new SetHandleItemContentEvent();
        res.mHandleItem = item;
        res.mContent = content;
        return res;
    }

    public HandleItem getHandleItem() {
        return mHandleItem;
    }

    public CharSequence getContent() {
        return mContent;
    }
}
