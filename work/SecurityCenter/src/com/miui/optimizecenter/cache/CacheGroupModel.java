
package com.miui.optimizecenter.cache;

import com.miui.optimizecenter.cache.StateButton.State;

public class CacheGroupModel {
    public CacheGroupModel() {

    }

    private String mAppName;
    private String mPackageName;
    private long mTotalSize;
    private State mState;

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(long totalSize) {
        if (totalSize == 0) {
            mTotalSize = 1;
        } else {
            mTotalSize = totalSize;
        }
    }

    @Override
    public String toString() {
        return "CacheModel : mAppName = " + mAppName + " mPackageName = " + mPackageName
                + " mState = " + mState + " mTotalSize = " + mTotalSize;
    }
}
