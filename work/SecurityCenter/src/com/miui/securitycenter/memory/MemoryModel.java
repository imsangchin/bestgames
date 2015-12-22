
package com.miui.securitycenter.memory;

import android.graphics.drawable.Drawable;

public class MemoryModel {

    private String mAppName;
    private String mPackageName;
    private long mMemorySize;
    private Drawable mAppIcon;
    private boolean mLocked;

    public MemoryModel() {

    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String pkgName) {
        mPackageName = pkgName;
    }

    public long getMemorySize() {
        return mMemorySize;
    }

    public void setMemorySize(long memorySize) {
        mMemorySize = memorySize;
    }

    public Drawable getAppIcon() {
        return mAppIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        mAppIcon = appIcon;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    @Override
    public String toString() {
        return "ProcessModel : AppName = " + mAppName + " PkgName = " + mPackageName
                + " MemorySize = " + mMemorySize + " Locked = " + mLocked + " AppIcon = "
                + mAppIcon;
    }
}
