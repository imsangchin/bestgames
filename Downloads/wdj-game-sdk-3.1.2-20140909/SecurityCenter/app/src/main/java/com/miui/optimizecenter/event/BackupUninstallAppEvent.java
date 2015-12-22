
package com.miui.optimizecenter.event;

public class BackupUninstallAppEvent {
    private String mPkgName;

    private BackupUninstallAppEvent() {
        // ignore
    }

    public static BackupUninstallAppEvent create(String pkgName) {
        BackupUninstallAppEvent res = new BackupUninstallAppEvent();
        res.mPkgName = pkgName;
        return res;
    }

    public String getPkgName() {
        return mPkgName;
    }

}
