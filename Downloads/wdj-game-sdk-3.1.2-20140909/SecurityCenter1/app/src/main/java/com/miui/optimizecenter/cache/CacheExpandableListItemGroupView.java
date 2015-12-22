
package com.miui.optimizecenter.cache;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BaseExpandableListAdapter;
import com.miui.common.BaseExpandableListItemGroupView;
import com.miui.optimizecenter.cache.StateButton.OnStateChangeListener;
import com.miui.optimizecenter.cache.StateButton.State;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.ExpandListGroupEvent;
import com.miui.optimizecenter.event.ListGroupStateChangedEvent;

import miui.text.ExtraTextUtils;

import com.miui.securitycenter.R;

public class CacheExpandableListItemGroupView extends
        BaseExpandableListItemGroupView<CacheGroupModel> implements OnStateChangeListener {

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mContentView;
    private StateButton mStateButton;
    private ImageView mIndicatorCloseView;
    private ImageView mIndicatorOpenView;

    private CacheGroupModel mData;
    private int mGroupPos = BaseExpandableListAdapter.NO_POS;

    public CacheExpandableListItemGroupView(Context context) {
        this(context, null);
    }

    public CacheExpandableListItemGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mContentView = (TextView) findViewById(R.id.content);
        mStateButton = (StateButton) findViewById(R.id.state_btn);
        mIndicatorCloseView = (ImageView) findViewById(R.id.indicator_close);
        mIndicatorOpenView = (ImageView) findViewById(R.id.indicator_open);

        setOnClickListener(this);
    }

    @Override
    public void fillData(CacheGroupModel model, int groupPos) {
        mStateButton.setOnStateChangeListener(null);

        mData = model;
        mGroupPos = groupPos;

        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                model.getPackageName());

        mTitleView.setText(model.getAppName());
        mContentView.setText(ExtraTextUtils.formatFileSize(getContext(), model.getTotalSize()));

        mStateButton.setState(model.getState());

        mStateButton.setOnStateChangeListener(this);
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (expanded) {
            mIndicatorCloseView.setVisibility(View.GONE);
            mIndicatorOpenView.setVisibility(View.VISIBLE);
        } else {
            mIndicatorOpenView.setVisibility(View.GONE);
            mIndicatorCloseView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (mData != null && mGroupPos != BaseExpandableListAdapter.NO_POS) {
            mEventHandler.sendEventMessage(EventType.EVENT_EXPAND_LIST_GROUP,
                    ExpandListGroupEvent.create(mGroupPos, mData.getPackageName(), !mIsExpanded));
        }
    }

    @Override
    public void onStateChanged(View v, State state) {
        mEventHandler.sendEventMessage(EventType.EVENT_LIST_GROUP_STATE_CHANGED,
                ListGroupStateChangedEvent.create(mData.getPackageName(), mData.getAppName(),
                        mStateButton.getState()));
    }
}
