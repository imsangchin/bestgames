
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.apk.ApkModel;

public class CleanApkItemEvent {

    private ApkModel mData;

    private CleanApkItemEvent() {

    }

    public static CleanApkItemEvent create(ApkModel data) {
        CleanApkItemEvent res = new CleanApkItemEvent();
        res.mData = data;
        return res;
    }

    public ApkModel getData() {
        return mData;
    }
}
