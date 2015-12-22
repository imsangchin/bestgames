
package com.miui.securitycenter.system;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.securitycenter.event.EventType;

import com.miui.securitycenter.R;

public class SystemListItemView extends LinearLayout implements OnClickListener {
    private Button mMenuButton;
    private TextView mHintView;
    private TextView mTitleView;
    private TextView mSummaryView;
    private TextView mContentView;

    private SystemItemModel mModel;
    private EventHandler mEventHandler;

    public SystemListItemView(Context context) {
        this(context, null);
    }

    public SystemListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SystemListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMenuButton = (Button) findViewById(R.id.menu);
        mMenuButton.setOnClickListener(this);
        mHintView = (TextView) findViewById(R.id.hint);
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);
        mContentView = (TextView) findViewById(R.id.content);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(SystemItemModel model) {
        mModel = model;
        if (model.getProtectionType() == SystemType.DANGEROUS) {
            mMenuButton.setVisibility(View.VISIBLE);
            mMenuButton.setText(R.string.button_text_fix);
            mHintView.setVisibility(View.GONE);
            mTitleView.setVisibility(View.VISIBLE);
            mSummaryView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
            mTitleView.setText(model.getTitle());
            mSummaryView.setText(model.getSummary());
        }  else {
            mMenuButton.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mTitleView.setVisibility(View.GONE);
            mSummaryView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
            mContentView.setText(model.getTitle());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_SYSTEM_ITEM_CHECKED,
                        OnSystemItemClickEvent.create(mModel));
                break;
            default:
                break;
        }
    }
}
