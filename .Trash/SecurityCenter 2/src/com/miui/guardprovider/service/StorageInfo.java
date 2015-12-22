
package com.miui.guardprovider.service;

import android.os.Parcel;
import android.os.Parcelable;

public class StorageInfo implements Parcelable {

    public long total;

    public long free;

    public StorageInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(total);
        dest.writeLong(free);
    }

    public void readFromParcel(Parcel source) {
        total = source.readLong();
        free = source.readLong();
    }

    public static final Creator<StorageInfo> CREATOR = new Creator<StorageInfo>() {
        public StorageInfo createFromParcel(Parcel source) {
            return new StorageInfo(source);
        }

        public StorageInfo[] newArray(int size) {
            return new StorageInfo[size];
        }
    };

    private StorageInfo(Parcel source) {
        readFromParcel(source);
    }

}
