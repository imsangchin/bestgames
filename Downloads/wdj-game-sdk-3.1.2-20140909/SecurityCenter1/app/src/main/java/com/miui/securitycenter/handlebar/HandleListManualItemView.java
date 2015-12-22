
package com.miui.securitycenter.handlebar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;

import com.miui.securitycenter.R;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.OnManualItemClickEvent;

public class HandleListManualItemView extends HandleListBaseItemView {

    private Button mMenuButton;
    private TextView mTitleView;
    private TextView mSummaryView;

    private EventHandler mEventHandler;
    private HandleItemModel mModel;

    private Context mContext;

    public HandleListManualItemView(Context context) {
        this(context, null);
    }

    public HandleListManualItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HandleListManualItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mMenuButton = (Button) findViewById(R.id.menu);
        mMenuButton.setOnClickListener(this);
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(HandleItemModel model) {
        mModel = model;
        mTitleView.setText(model.getTitle());
        mSummaryView.setText(model.getSummary());
        mMenuButton.setText(getContext().getString(R.string.button_text_view));
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.menu:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_MANUAL_ITEM_CLICK,
                        OnManualItemClickEvent.create(mModel));
                break;
            default:
                break;
        }
    }
}
