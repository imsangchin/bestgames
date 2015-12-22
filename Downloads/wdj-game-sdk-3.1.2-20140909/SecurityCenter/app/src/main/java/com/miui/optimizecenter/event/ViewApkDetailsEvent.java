
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.apk.ApkModel;

public class ViewApkDetailsEvent {

    private ApkModel mData;

    private ViewApkDetailsEvent() {

    }

    public static ViewApkDetailsEvent create(ApkModel data) {
        ViewApkDetailsEvent res = new ViewApkDetailsEvent();
        res.mData = data;
        return res;
    }

    public ApkModel getData() {
        return mData;
    }
}
