
package com.miui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;

public class BaseExpandableListItemChildView<D> extends FrameLayout implements
        BindableView<D>, OnClickListener, OnCheckedChangeListener {

    public BaseExpandableListItemChildView(Context context) {
        this(context, null);
    }

    public BaseExpandableListItemChildView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseExpandableListItemChildView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected EventHandler mEventHandler;

    @Override
    public void fillData(D data) {
        // ignore
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onClick(View v) {
        // ignore
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // ignore
    }
}
