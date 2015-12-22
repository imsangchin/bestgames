
package com.miui.optimizecenter.residual;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ResidualModel implements Serializable {

    private static final long serialVersionUID = -4147855146735716066L;

    public ResidualModel() {

    }

    private String mDirectoryPath;
    private String mDescName;
    private boolean mAdviseDelete;
    private String mAlertInfo;
    private long mFileSize;
    private Drawable mFileIcon;

    public String getDirectoryPath() {
        return mDirectoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        mDirectoryPath = directoryPath;
    }

    public String getDescName() {
        return mDescName;
    }

    public void setDescName(String descName) {
        mDescName = descName;
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

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        if (fileSize == 0) {
            mFileSize = 4 * 1000;
        } else {
            mFileSize = fileSize;
        }
    }

    public void setFileIcon(Drawable icon) {
        mFileIcon = icon;
    }

    public Drawable getFileIcon() {
        return mFileIcon;
    }

    @Override
    public String toString() {
        return "ResidualModel : mDirectoryPath = " + mDirectoryPath + " mDescName = " + mDescName
                + " mAdviseDelete = " + mAdviseDelete + " mAlertInfo = " + mAlertInfo
                + " FileIcon = " + mFileIcon;
    }
}
