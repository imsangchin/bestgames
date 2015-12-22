
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.residual.ResidualModel;

public class CleanResidualEvent {

    private ResidualModel mData;

    private CleanResidualEvent() {

    }

    public static CleanResidualEvent create(ResidualModel data) {
        CleanResidualEvent res = new CleanResidualEvent();
        res.mData = data;
        return res;
    }

    public ResidualModel getData() {
        return mData;
    }
}
