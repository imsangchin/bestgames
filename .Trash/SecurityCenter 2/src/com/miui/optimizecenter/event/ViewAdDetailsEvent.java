
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.ad.AdModel;

public class ViewAdDetailsEvent {

    private AdModel mData;

    private ViewAdDetailsEvent() {

    }

    public static ViewAdDetailsEvent create(AdModel data) {
        ViewAdDetailsEvent res = new ViewAdDetailsEvent();
        res.mData = data;
        return res;
    }

    public AdModel getData() {
        return mData;
    }
}
