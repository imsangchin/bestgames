
package com.miui.permcenter.autostart;

public class AutoStartModel {

    private String mPkgName;

    private String mAppLabel;

    private String mWarningInfo;

    private boolean mAutoStartEnabled;

    public AutoStartModel() {
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

    public void setWarningInfo(String waningInfo) {
        mWarningInfo = waningInfo;
    }

    public String getWarningInfo() {
        return mWarningInfo;
    }

    public void setAutoStartEnabled(boolean enabled) {
        mAutoStartEnabled = enabled;
    }

    public boolean isAutoStartEnabled() {
        return mAutoStartEnabled;
    }

    @Override
    public String toString() {
        return "AutoStartModel mPkgName = " + mPkgName + " mAppLabel = " + mAppLabel
                + " mWarningInfo = " + mWarningInfo + " mAutoStartEnabled = " + mAutoStartEnabled;
    }
}
