
package com.miui.optimizecenter.ad;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class AdModel implements Serializable {

    private static final long serialVersionUID = -8283250484217024358L;

    public AdModel() {

    }

    private String mName;
    private String mDirectoryPath;
    private long mFileSize;
    private boolean mAdviseDelete;
    private Drawable mFileIcon;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDirectoryPath() {
        return mDirectoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        mDirectoryPath = directoryPath;
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

    public void setAdviseDelete(boolean adviseDel) {
        mAdviseDelete = adviseDel;
    }

    public boolean adviseDelete() {
        return mAdviseDelete;
    }

    public void setFileIcon(Drawable icon) {
        mFileIcon = icon;
    }

    public Drawable getFileIcon() {
        return mFileIcon;
    }

    @Override
    public String toString() {
        return "AdModel : name = " + mName + " DirectoryPath = " + mDirectoryPath
                + " AdviseDelete = " + mAdviseDelete + " FileIcon = " + mFileIcon;
    }
}
