
package com.miui.optimizecenter.apk;

import android.graphics.drawable.Drawable;

import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.SecurityStatus;

import java.io.Serializable;

public class ApkModel implements Serializable {

    private static final long serialVersionUID = -1530638368675292939L;

    public ApkModel() {
        // ignore
    }

    private Drawable mLauncher;
    private String mApplicationLabel;
    private ApkStatus mStatus;
    private String mPackageName;
    private String mVersionName;
    private int mVersionCode;
    private String mAbsolutePath;
    private long mFileSize;
    private boolean mAdviseDelete;
    private SecurityStatus mSecurityStatus;
    private String mSecurityInfo;

    public Drawable getLauncher() {
        return mLauncher;
    }

    public void setLauncher(Drawable launcher) {
        mLauncher = launcher;
    }

    public String getApplicationLabel() {
        return mApplicationLabel;
    }

    public void setApplicationLabel(String label) {
        mApplicationLabel = label;
    }

    public ApkStatus getStatus() {
        return mStatus;
    }

    public void setStatus(ApkStatus status) {
        mStatus = status;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public void setVersionName(String versionName) {
        mVersionName = versionName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(int versionCode) {
        mVersionCode = versionCode;
    }

    public String getAbsolutePath() {
        return mAbsolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        mAbsolutePath = absolutePath;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    public void setAdviseDelete(boolean adviseDel) {
        mAdviseDelete = adviseDel;
    }

    public boolean adviseDelete() {
        return mAdviseDelete;
    }

    public void setSecurityStatus(SecurityStatus status) {
        mSecurityStatus = status;
    }

    public SecurityStatus getSecurityStatus() {
        return mSecurityStatus;
    }

    public void setSecurityInfo(String info) {
        mSecurityInfo = info;
    }

    public String getSecurityInfo() {
        return mSecurityInfo;
    }

    @Override
    public String toString() {
        return "ApkModel : PackageName = " + mPackageName + " VersionName = " + mVersionName
                + " VersionCode = " + mVersionCode + " AbsolutePath = " + mAbsolutePath
                + " status = " + mStatus + " Launcher = " + mLauncher + " AdviseDelete = "
                + mAdviseDelete + " mSecurityStatus = " + mSecurityStatus + " mSecurityInfo = "
                + mSecurityInfo;
    }
}
