
package com.miui.optimizecenter.cache;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class CacheModel implements Serializable {
    private static final long serialVersionUID = 7669487277584987395L;

    public CacheModel() {

    }

    private Drawable mLauncher;
    private String mCacheType;
    private String mDirectoryPath;
    private String mPackageName;
    private boolean mAdviseDelete;
    private String mAlertInfo;
    private String mDescription;
    private long mFileSize;

    public Drawable getLauncher() {
        return mLauncher;
    }

    public void setLauncher(Drawable launcher) {
        mLauncher = launcher;
    }

    public String getCacheType() {
        return mCacheType;
    }

    public void setCacheType(String cacheType) {
        mCacheType = cacheType;
    }

    public String getDirectoryPath() {
        return mDirectoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        mDirectoryPath = directoryPath;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public boolean adviseDelete() {
        return mAdviseDelete;
    }

    public void setAdviseDelete(boolean adviseDelete) {
        mAdviseDelete = adviseDelete;
    }

    public String getAlertInfo() {
        return mAlertInfo;
    }

    public void setAlertInfo(String alertInfo) {
        mAlertInfo = alertInfo;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    @Override
    public String toString() {
        return "CacheModel : mCacheType = " + mCacheType + " mDirectoryPath = " + mDirectoryPath
                + " mPackageName = " + mPackageName + " mAdviseDelete = " + mAdviseDelete
                + " mAlertInfo = " + mAlertInfo + " mDescription = " + mDescription
                + " Launcher = " + mLauncher;
    }
}
