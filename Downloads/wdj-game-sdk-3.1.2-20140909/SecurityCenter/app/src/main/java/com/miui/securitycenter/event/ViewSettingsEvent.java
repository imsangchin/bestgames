
package com.miui.securitycenter.event;

public class ViewSettingsEvent {

    private ViewSettingsEvent() {
        // ingore
    }

    public static ViewSettingsEvent create() {
        return new ViewSettingsEvent();
    }

}
