
package com.miui.antivirus.event;

import com.miui.antivirus.VirusModel;

public class OnVirusListItemClickEvent {

    private VirusModel mData;

    private OnVirusListItemClickEvent() {
        // ignore
    }

    public static OnVirusListItemClickEvent create(VirusModel data) {
        OnVirusListItemClickEvent res = new OnVirusListItemClickEvent();
        res.mData = data;
        return res;
    }

    public VirusModel getData() {
        return mData;
    }
}
