
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.deepclean.InstalledAppModel;

public class ViewInstalledAppDetailsEvent {

    private InstalledAppModel mData;

    private ViewInstalledAppDetailsEvent() {

    }

    public static ViewInstalledAppDetailsEvent create(InstalledAppModel data) {
        ViewInstalledAppDetailsEvent res = new ViewInstalledAppDetailsEvent();
        res.mData = data;
        return res;
    }

    public InstalledAppModel getData() {
        return mData;
    }
}
