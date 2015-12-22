
package com.miui.permcenter.event;

public class OnPermAppsItemClickEvent {

    private String mPkgName;

    private OnPermAppsItemClickEvent() {
        // ignore
    }

    public static OnPermAppsItemClickEvent create(String pkgName) {
        OnPermAppsItemClickEvent res = new OnPermAppsItemClickEvent();
        res.mPkgName = pkgName;
        return res;
    }

    public String getPkgName() {
        return mPkgName;
    }
}
