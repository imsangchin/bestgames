
package com.miui.antivirus.event;

public class OnFinishCloudScanVirusEvent {

    private OnFinishCloudScanVirusEvent() {
        // ignore
    }

    public static OnFinishCloudScanVirusEvent create() {
        return new OnFinishCloudScanVirusEvent();
    }
}
