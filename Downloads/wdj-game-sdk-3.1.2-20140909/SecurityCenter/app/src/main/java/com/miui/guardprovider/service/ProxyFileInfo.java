
package com.miui.guardprovider.service;

import java.io.File;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ProxyFileInfo implements Parcelable {

    private String mName;

    private String mParent;

    private String mPath;

    private String mAbsolutePath;

    private boolean mIsFile;

    private boolean mIsDirectory;

    private long mSize;

    private boolean mIsExists;

    private String mFileUri;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getParent() {
        return mParent;
    }

    public void setParent(String parent) {
        mParent = parent;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getAbsolutePath() {
        return mAbsolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        mAbsolutePath = absolutePath;
    }

    public boolean isDirectory() {
        return mIsDirectory;
    }

    public void setIsDirectory(boolean value) {
        mIsDirectory = value;
    }

    public boolean isFile() {
        return mIsFile;
    }

    public void setIsFile(boolean value) {
        mIsFile = value;
    }

    public boolean exists() {
        return mIsExists;
    }

    public void setExists(boolean exists) {
        mIsExists = exists;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getFileUri() {
        return mFileUri;
    }

    public void setFileUri(String uri) {
        mFileUri = uri;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mParent);
        dest.writeString(mPath);
        dest.writeString(mAbsolutePath);
        dest.writeInt(mIsFile ? 1 : 0);
        dest.writeInt(mIsDirectory ? 1 : 0);
        dest.writeInt(mIsExists ? 1 : 0);
        dest.writeLong(mSize);
        dest.writeString(mFileUri);
    }

    public ProxyFileInfo() {
    }

    public ProxyFileInfo(File file) {
        mName = file.getName();
        mParent = file.getParent();
        mPath = file.getPath();
        mAbsolutePath = file.getAbsolutePath();
        mIsFile = file.isFile();
        mIsDirectory = file.isDirectory();
        mIsExists = file.exists();
        mSize = file.length();
        mFileUri = Uri.fromFile(file).getPath();
    }

    public ProxyFileInfo(Parcel in) {
        mName = in.readString();
        mParent = in.readString();
        mPath = in.readString();
        mAbsolutePath = in.readString();
        mIsFile = (in.readInt() == 1);
        mIsDirectory = (in.readInt() == 1);
        mIsExists = (in.readInt() == 1);
        mSize = in.readLong();
        mFileUri = in.readString();
    }

    public static final Parcelable.Creator<ProxyFileInfo> CREATOR = new Parcelable.Creator<ProxyFileInfo>() {
        public ProxyFileInfo createFromParcel(Parcel in) {
            return new ProxyFileInfo(in);
        }

        public ProxyFileInfo[] newArray(int size) {
            return new ProxyFileInfo[size];
        }
    };

}
