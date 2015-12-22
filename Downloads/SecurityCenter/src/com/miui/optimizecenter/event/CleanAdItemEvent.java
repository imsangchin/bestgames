
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.ad.AdModel;

public class CleanAdItemEvent {

    private AdModel mData;

    private CleanAdItemEvent() {

    }

    public static CleanAdItemEvent create(AdModel data) {
        CleanAdItemEvent res = new CleanAdItemEvent();
        res.mData = data;
        return res;
    }

    public AdModel getData() {
        return mData;
    }
}
