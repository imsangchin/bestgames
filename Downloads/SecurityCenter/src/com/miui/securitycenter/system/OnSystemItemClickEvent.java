
package com.miui.securitycenter.system;



public class OnSystemItemClickEvent {
    private SystemItemModel mModel;

    private OnSystemItemClickEvent() {

    }

    public static OnSystemItemClickEvent create(SystemItemModel model) {
        OnSystemItemClickEvent res = new OnSystemItemClickEvent();
        res.mModel = model;
        return res;
    }

    public SystemItemModel getProtectionModel() {
        return mModel;
    }
}
