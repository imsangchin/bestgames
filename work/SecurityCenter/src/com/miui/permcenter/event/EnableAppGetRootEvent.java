
package com.miui.permcenter.event;

public class EnableAppGetRootEvent {

    private String mPkgName;
    private boolean mEnabled;

    private EnableAppGetRootEvent() {
        // ignore
    }

    public static EnableAppGetRootEvent create(String pkgName, boolean enabled) {
        EnableAppGetRootEvent res = new EnableAppGetRootEvent();
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
