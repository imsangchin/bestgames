
package com.miui.permcenter.event;

public class EnableAppAutoStartEvent {

    private String mPkgName;
    private boolean mEnabled;

    private EnableAppAutoStartEvent() {
        // ignore
    }

    public static EnableAppAutoStartEvent create(String pkgName, boolean enabled) {
        EnableAppAutoStartEvent res = new EnableAppAutoStartEvent();
        res.mPkgName = pkgName;
        res.mEnabled = enabled;
        return res;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
}
