
package com.miui.antivirus.event;

import com.miui.antivirus.VirusModel;

public class OnFindVirusItemEvent {

    private VirusModel mVirusModel;

    private OnFindVirusItemEvent() {
        // ignore
    }

    public static OnFindVirusItemEvent create(VirusModel model) {
        OnFindVirusItemEvent res = new OnFindVirusItemEvent();
        res.mVirusModel = model;
        return res;
    }

    public VirusModel getVirusModel() {
        return mVirusModel;
    }
}
