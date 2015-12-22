
package com.miui.optimizecenter.deepclean;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class LargeFileModel implements Serializable {

    private static final long serialVersionUID = -4413904860178295142L;

    public static final long MIN_SIZE = 10 * 1024 * 1024;

    private long mSize;
    private String mName;
    private String mPath;
    private Drawable mIcon;
    private long mFileSize;
    private boolean mAdviseDelete;

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    public boolean adviseDelete() {
        return mAdviseDelete;
    }

    public void setAdviseDelete(boolean adviseDelete) {
        mAdviseDelete = adviseDelete;
    }

    @Override
    public String toString() {
        return "LargeFileModel : Name = " + mName + " Path = " + mPath + " Icon = " + mIcon
                + " Size = " + mFileSize + " AdviseDelete = " + mAdviseDelete;
    }
}
