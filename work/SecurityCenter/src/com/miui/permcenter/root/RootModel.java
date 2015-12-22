
package com.miui.permcenter.root;

public class RootModel {

    private String mPkgName;

    private String mAppLabel;

    private boolean mRootEnabled;

    public RootModel() {
        // ignore
    }

    public void setPkgName(String pkgName) {
        mPkgName = pkgName;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public void setAppLabel(String appLabel) {
        mAppLabel = appLabel;
    }

    public String getAppLabel() {
        return mAppLabel;
    }

    public void setRootEnabled(boolean enabled) {
        mRootEnabled = enabled;
    }

    public boolean isRootEnabled() {
        return mRootEnabled;
    }

    @Override
    public String toString() {
        return "AutoStartModel mPkgName = " + mPkgName + " mAppLabel = " + mAppLabel
                + " mRootEnabled = " + mRootEnabled;
    }
}
