package com.miui.guardprovider.service;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class ProxyPackageInfo implements Parcelable {

    private PackageInfo mPackageInfo;

    private String mLabel;

    public ProxyPackageInfo(PackageInfo packageInfo) {
        mPackageInfo = packageInfo;
    }

    public ProxyPackageInfo(Parcel in) {
        mPackageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
        mLabel = in.readString();
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPackageInfo, flags);
        dest.writeString(mLabel);
    }

    public static final Parcelable.Creator<ProxyPackageInfo> CREATOR = new Parcelable.Creator<ProxyPackageInfo>() {
        public ProxyPackageInfo createFromParcel(Parcel in) {
            return new ProxyPackageInfo(in);
        }

        public ProxyPackageInfo[] newArray(int size) {
            return new ProxyPackageInfo[size];
        }
    };
}
