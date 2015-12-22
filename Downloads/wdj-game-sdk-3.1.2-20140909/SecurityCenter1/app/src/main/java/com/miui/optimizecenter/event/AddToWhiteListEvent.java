
package com.miui.optimizecenter.event;

public class AddToWhiteListEvent {

    private Object mData;

    private AddToWhiteListEvent() {

    }

    public static AddToWhiteListEvent create(Object data) {
        AddToWhiteListEvent res = new AddToWhiteListEvent();
        res.mData = data;
        return res;
    }

    public Object getData() {
        return mData;
    }
}
