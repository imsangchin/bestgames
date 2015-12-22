
package com.miui.optimizecenter.event;

import android.view.View;

public class PerformItemClickEvent {
    private View mView;
    private int mPosition;

    private PerformItemClickEvent() {

    }

    public static PerformItemClickEvent create(View view, int pos) {
        PerformItemClickEvent res = new PerformItemClickEvent();
        res.mView = view;
        res.mPosition = pos;
        return res;
    }

    public View getView() {
        return mView;
    }

    public int getPosition() {
        return mPosition;
    }
}
