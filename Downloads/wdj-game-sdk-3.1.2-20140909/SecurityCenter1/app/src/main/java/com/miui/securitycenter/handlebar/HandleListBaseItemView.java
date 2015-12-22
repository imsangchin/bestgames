
package com.miui.securitycenter.handlebar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.miui.common.EventHandler;

public class HandleListBaseItemView extends LinearLayout implements OnClickListener {

    public HandleListBaseItemView(Context context) {
        this(context, null);
    }

    public HandleListBaseItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public HandleListBaseItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void fillData(HandleItemModel model) {
        // ignore
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
    }

    public void setEventHandler(EventHandler mHandler) {
        // ignore
    }

}
