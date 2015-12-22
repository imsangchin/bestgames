
package com.miui.securitycenter.event;

public class NotifyWhiteListHeader {

    private boolean mShow;

    public NotifyWhiteListHeader() {
        // ignore
    }

    public static NotifyWhiteListHeader create(boolean shown) {
        NotifyWhiteListHeader res = new NotifyWhiteListHeader();
        res.mShow = shown;
        return res;
    }

    public boolean isShown(){
        return mShow;
    }
}
