
package com.miui.antivirus.event;

import com.miui.antivirus.VirusModel;

public class OnRiskListItemClickEvent {
    private VirusModel mData;

    private OnRiskListItemClickEvent() {
        // ignore
    }

    public static OnRiskListItemClickEvent create(VirusModel data) {
        OnRiskListItemClickEvent res = new OnRiskListItemClickEvent();
        res.mData = data;
        return res;
    }

    public VirusModel getData() {
        return mData;
    }
}
