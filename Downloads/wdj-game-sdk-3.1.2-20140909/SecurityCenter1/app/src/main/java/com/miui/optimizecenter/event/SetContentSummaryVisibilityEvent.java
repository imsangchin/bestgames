
package com.miui.optimizecenter.event;

public class SetContentSummaryVisibilityEvent {

    private int mVisibility;

    private SetContentSummaryVisibilityEvent() {
        // ignore
    }

    public static SetContentSummaryVisibilityEvent create(int visibility) {
        SetContentSummaryVisibilityEvent res = new SetContentSummaryVisibilityEvent();
        res.mVisibility = visibility;
        return res;
    }

    public int getVisibility() {
        return mVisibility;
    }
}
