
package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItemModel;;

public class OnManualItemClickEvent {
    private HandleItemModel mModel;

    private OnManualItemClickEvent() {
    }

    public static OnManualItemClickEvent create(HandleItemModel model) {
        OnManualItemClickEvent res = new OnManualItemClickEvent();
        res.mModel = model;
        return res;
    }

    public HandleItemModel getModel() {
        return mModel;
    }
}
