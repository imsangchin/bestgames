
package com.miui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class BaseExpandableListItemGroupView<D> extends LinearLayout implements
        BindableGroupView<D>, OnCheckedChangeListener, OnClickListener {
    protected EventHandler mEventHandler;
    protected boolean mIsExpanded = false;

    public BaseExpandableListItemGroupView(Context context) {
        this(context, null);
    }

    public BaseExpandableListItemGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void fillData(D data, int groupPos) {
        // ignore
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void setExpanded(boolean expanded) {
        mIsExpanded = expanded;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // ignore
    }

    @Override
    public void onClick(View v) {
        // ignore
    }
}
