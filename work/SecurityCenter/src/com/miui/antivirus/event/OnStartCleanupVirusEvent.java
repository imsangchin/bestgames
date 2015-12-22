
package com.miui.antivirus.event;

public class OnStartCleanupVirusEvent {

    private OnStartCleanupVirusEvent() {
        // ignore
    }

    public static OnStartCleanupVirusEvent create() {
        return new OnStartCleanupVirusEvent();
    }
}
