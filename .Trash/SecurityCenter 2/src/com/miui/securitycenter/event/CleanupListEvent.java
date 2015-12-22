
package com.miui.securitycenter.event;

public class CleanupListEvent {

    private CleanupListEvent() {

    }

    public static CleanupListEvent create() {
        return new CleanupListEvent();
    }

}
