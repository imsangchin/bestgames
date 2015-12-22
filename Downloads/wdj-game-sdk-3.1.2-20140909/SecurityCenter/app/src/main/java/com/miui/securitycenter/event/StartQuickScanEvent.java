
package com.miui.securitycenter.event;

public class StartQuickScanEvent {

    private StartQuickScanEvent() {
        // ingore
    }

    public static StartQuickScanEvent create() {
        return new StartQuickScanEvent();
    }

}
