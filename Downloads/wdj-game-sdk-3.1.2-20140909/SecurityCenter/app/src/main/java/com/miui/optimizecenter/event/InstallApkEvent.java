
package com.miui.optimizecenter.event;

public class InstallApkEvent {

    private String mPath;

    private InstallApkEvent() {

    }

    public static InstallApkEvent create(String path) {
        InstallApkEvent res = new InstallApkEvent();
        res.mPath = path;
        return res;
    }

    public String getPath() {
        return mPath;
    }
}
