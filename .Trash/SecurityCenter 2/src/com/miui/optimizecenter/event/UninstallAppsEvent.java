
package com.miui.optimizecenter.event;

public class UninstallAppsEvent {

    private UninstallAppsEvent() {
        // ignore
    }

    public static UninstallAppsEvent create() {
        return new UninstallAppsEvent();
    }

}
