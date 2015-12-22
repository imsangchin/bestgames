
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.deepclean.LargeFileModel;

public class ViewLargeFileDetailsEvent {

    private LargeFileModel mData;

    private ViewLargeFileDetailsEvent() {

    }

    public static ViewLargeFileDetailsEvent create(LargeFileModel data) {
        ViewLargeFileDetailsEvent res = new ViewLargeFileDetailsEvent();
        res.mData = data;
        return res;
    }

    public LargeFileModel getData() {
        return mData;
    }
}
