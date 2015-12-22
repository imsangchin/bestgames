
package com.miui.securitycenter.event;

public class RefreshManualItemsEvent {

    private RefreshManualItemsEvent() {
        // ignore
    }

    public static RefreshManualItemsEvent create() {
        return new RefreshManualItemsEvent();
    }

}
