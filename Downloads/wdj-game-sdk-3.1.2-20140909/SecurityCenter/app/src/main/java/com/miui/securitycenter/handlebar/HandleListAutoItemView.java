
package com.miui.securitycenter.handlebar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.securitycenter.R;

public class HandleListAutoItemView extends HandleListBaseItemView {

    private TextView mTitle;
    private TextView mContent;
    private HandleItemModel mModel;
    private EventHandler mEventHandler;

    public HandleListAutoItemView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public HandleListAutoItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public HandleListAutoItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
        mContent = (TextView) findViewById(R.id.content);
    }

    public void fillData(HandleItemModel model) {
        mModel = model;
        mTitle.setText(model.getTitle());
        mContent.setText(model.getSummary());
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }
}
