
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.deepclean.LargeFileModel;

public class CleanLargeFileEvent {

    private LargeFileModel mData;
    private boolean mIsSilent;

    private CleanLargeFileEvent() {

    }

    public static CleanLargeFileEvent create(LargeFileModel data, boolean silent) {
        CleanLargeFileEvent res = new CleanLargeFileEvent();
        res.mData = data;
        res.mIsSilent = silent;
        return res;
    }

    public LargeFileModel getData() {
        return mData;
    }

    public boolean isSilent() {
        return mIsSilent;
    }
}
