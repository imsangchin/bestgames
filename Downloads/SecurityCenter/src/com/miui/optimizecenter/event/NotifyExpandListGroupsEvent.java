
package com.miui.optimizecenter.event;

public class NotifyExpandListGroupsEvent {
    private boolean mIsExpand;

    private NotifyExpandListGroupsEvent() {

    }

    public static NotifyExpandListGroupsEvent create(boolean isExpand) {
        NotifyExpandListGroupsEvent res = new NotifyExpandListGroupsEvent();
        res.mIsExpand = isExpand;
        return res;
    }

    public boolean isExpand() {
        return mIsExpand;
    }
}
