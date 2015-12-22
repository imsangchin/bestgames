
package com.miui.securitycenter.event;

public class OnBackPressedEvent {

    private OnBackPressedEvent() {
        // ingore
    }

    public static OnBackPressedEvent create() {
        return new OnBackPressedEvent();
    }

}
