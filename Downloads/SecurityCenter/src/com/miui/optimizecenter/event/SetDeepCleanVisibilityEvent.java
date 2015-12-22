
package com.miui.optimizecenter.event;

public class SetDeepCleanVisibilityEvent {

    private int mVisibility;

    private SetDeepCleanVisibilityEvent() {
        // ignore
    }

    public static SetDeepCleanVisibilityEvent create(int visibility) {
        SetDeepCleanVisibilityEvent res = new SetDeepCleanVisibilityEvent();
        res.mVisibility = visibility;
        return res;
    }

    public int getVisibility() {
        return mVisibility;
    }
}
