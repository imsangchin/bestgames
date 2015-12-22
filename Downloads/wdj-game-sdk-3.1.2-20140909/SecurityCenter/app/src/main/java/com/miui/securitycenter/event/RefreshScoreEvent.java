
package com.miui.securitycenter.event;

public class RefreshScoreEvent {

    private RefreshScoreEvent() {
        // ingore
    }

    public static RefreshScoreEvent create() {
        return new RefreshScoreEvent();
    }

}
