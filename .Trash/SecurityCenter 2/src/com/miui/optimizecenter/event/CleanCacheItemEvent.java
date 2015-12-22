
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.cache.CacheModel;

public class CleanCacheItemEvent {

    private CacheModel mData;

    private CleanCacheItemEvent() {

    }

    public static CleanCacheItemEvent create(CacheModel data) {
        CleanCacheItemEvent res = new CleanCacheItemEvent();
        res.mData = data;
        return res;
    }

    public CacheModel getData() {
        return mData;
    }
}
