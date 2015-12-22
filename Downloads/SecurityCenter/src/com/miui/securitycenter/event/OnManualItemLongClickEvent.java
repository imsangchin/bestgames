package com.miui.securitycenter.event;

import com.miui.securitycenter.handlebar.HandleItemModel;

public class OnManualItemLongClickEvent {

    private HandleItemModel model;

    private OnManualItemLongClickEvent() {
        //ignore
    }

    public static OnManualItemLongClickEvent create(HandleItemModel model) {
        OnManualItemLongClickEvent res = new OnManualItemLongClickEvent();
        res.model = model;
        return res;
    }

    public HandleItemModel getModel() {
        return model;
    }
}
