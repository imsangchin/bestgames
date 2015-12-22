
package com.miui.securitycenter.cache;

import android.graphics.drawable.Drawable;

public class CacheModel {

    private String mAppName;
    private String mPackageName;
    private long mCacheSize;
    private Drawable mAppIcon;
    private boolean mLocked;

    public CacheModel() {

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

    public long getCacheSize() {
        return mCacheSize;
    }

    public void setCacheSize(long cacheSize) {
        mCacheSize = cacheSize;
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
        return "CacheModel : AppName = " + mAppName + " PkgName = " + mPackageName
                + " CacheSize = " + mCacheSize + " Locked = " + mLocked + " AppIcon = " + mAppIcon;
    }
}
