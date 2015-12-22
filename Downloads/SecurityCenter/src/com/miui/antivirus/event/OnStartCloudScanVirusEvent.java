
package com.miui.antivirus.event;

public class OnStartCloudScanVirusEvent {

    private OnStartCloudScanVirusEvent() {
        // ignore
    }

    public static OnStartCloudScanVirusEvent create() {
        return new OnStartCloudScanVirusEvent();
    }
}
