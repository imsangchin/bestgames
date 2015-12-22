
package com.miui.antivirus.event;

public class OnStartScanVirusEvent {

    private OnStartScanVirusEvent() {
        // ignore
    }

    public static OnStartScanVirusEvent create() {
        return new OnStartScanVirusEvent();
    }
}
