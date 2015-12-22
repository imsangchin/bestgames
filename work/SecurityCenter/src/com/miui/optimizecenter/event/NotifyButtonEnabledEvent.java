
package com.miui.optimizecenter.event;

public class NotifyButtonEnabledEvent {
    private boolean mEnabled;

    private NotifyButtonEnabledEvent() {

    }

    public static NotifyButtonEnabledEvent create(boolean enabled) {
        NotifyButtonEnabledEvent res = new NotifyButtonEnabledEvent();
        res.mEnabled = enabled;
        return res;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
}
