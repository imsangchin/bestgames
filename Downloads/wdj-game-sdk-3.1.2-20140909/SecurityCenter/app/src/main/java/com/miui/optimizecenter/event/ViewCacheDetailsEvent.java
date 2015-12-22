
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.cache.CacheModel;

public class ViewCacheDetailsEvent {

    private CacheModel mData;

    private ViewCacheDetailsEvent() {

    }

    public static ViewCacheDetailsEvent create(CacheModel data) {
        ViewCacheDetailsEvent res = new ViewCacheDetailsEvent();
        res.mData = data;
        return res;
    }

    public CacheModel getData() {
        return mData;
    }
}
