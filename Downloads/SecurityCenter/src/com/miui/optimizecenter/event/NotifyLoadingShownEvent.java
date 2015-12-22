
package com.miui.optimizecenter.event;

public class NotifyLoadingShownEvent {
    private boolean mShown;

    private NotifyLoadingShownEvent() {

    }

    public static NotifyLoadingShownEvent create(boolean shown) {
        NotifyLoadingShownEvent res = new NotifyLoadingShownEvent();
        res.mShown = shown;
        return res;
    }

    public boolean isShown() {
        return mShown;
    }
}
