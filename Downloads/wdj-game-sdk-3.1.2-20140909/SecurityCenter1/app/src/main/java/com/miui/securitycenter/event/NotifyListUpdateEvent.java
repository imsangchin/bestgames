
package com.miui.securitycenter.event;

public class NotifyListUpdateEvent {
    private boolean mNeedSort;

    private NotifyListUpdateEvent() {

    }

    public static NotifyListUpdateEvent create(boolean needSort) {
        NotifyListUpdateEvent res = new NotifyListUpdateEvent();
        res.mNeedSort = needSort;
        return res;
    }

    public boolean isNeedSort() {
        return mNeedSort;
    }
}
