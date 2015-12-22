
package com.miui.antivirus.event;

public class OnFinishCleanupVirusEvent {

    private OnFinishCleanupVirusEvent() {
        // ignore
    }

    public static OnFinishCleanupVirusEvent create() {
        return new OnFinishCleanupVirusEvent();
    }
}
