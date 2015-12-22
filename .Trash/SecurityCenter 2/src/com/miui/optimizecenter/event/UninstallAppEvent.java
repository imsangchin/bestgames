
package com.miui.optimizecenter.event;

public class UninstallAppEvent {
    private String mPkgName;
    private boolean mSilent;

    private UninstallAppEvent() {
        // ignore
    }

    public static UninstallAppEvent create(String pkgName, boolean silent) {
        UninstallAppEvent res = new UninstallAppEvent();
        res.mPkgName = pkgName;
        res.mSilent = silent;
        return res;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public boolean isSilent() {
        return mSilent;
    }

}
