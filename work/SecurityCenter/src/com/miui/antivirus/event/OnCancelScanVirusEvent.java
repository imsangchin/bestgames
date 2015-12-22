
package com.miui.antivirus.event;

public class OnCancelScanVirusEvent {

    private OnCancelScanVirusEvent() {
        // ignore
    }

    public static OnCancelScanVirusEvent create() {
        return new OnCancelScanVirusEvent();
    }
}
