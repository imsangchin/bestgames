
package com.miui.antivirus.event;

public class OnFinishScanVirusEvent {

    private OnFinishScanVirusEvent() {
        // ignore
    }

    public static OnFinishScanVirusEvent create() {
        return new OnFinishScanVirusEvent();
    }
}
