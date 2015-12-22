
package com.miui.securitycenter.cache;


public class CacheListItemCheckedEvent {
    private boolean mChecked;
    private CacheModel mCacheModel;

    private CacheListItemCheckedEvent() {

    }

    public static CacheListItemCheckedEvent create(boolean checked, CacheModel model) {
        CacheListItemCheckedEvent res = new CacheListItemCheckedEvent();
        res.mChecked = checked;
        res.mCacheModel = model;

        return res;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public CacheModel getCacheModel() {
        return mCacheModel;
    }
}
