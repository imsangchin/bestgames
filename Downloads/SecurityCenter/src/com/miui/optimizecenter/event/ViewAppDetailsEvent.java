
package com.miui.optimizecenter.event;

public class ViewAppDetailsEvent {
    private String mPkgName;

    private ViewAppDetailsEvent() {
        // ignore
    }

    public static ViewAppDetailsEvent create(String pkgName) {
        ViewAppDetailsEvent res = new ViewAppDetailsEvent();
        res.mPkgName = pkgName;
        return res;
    }

    public String getPkgName() {
        return mPkgName;
    }

}
