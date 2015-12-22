
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.residual.ResidualModel;

public class ViewResidualDetailsEvent {

    private ResidualModel mData;

    private ViewResidualDetailsEvent() {

    }

    public static ViewResidualDetailsEvent create(ResidualModel data) {
        ViewResidualDetailsEvent res = new ViewResidualDetailsEvent();
        res.mData = data;
        return res;
    }

    public ResidualModel getData() {
        return mData;
    }
}
