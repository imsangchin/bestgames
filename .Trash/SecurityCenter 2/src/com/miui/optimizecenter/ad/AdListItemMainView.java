
package com.miui.optimizecenter.ad;

import miui.text.ExtraTextUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView.ListItemCollapseListener;
import com.miui.common.ExpandableListView.ListItemExpandListener;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.ViewAdDetailsEvent;

import com.miui.securitycenter.R;

public class AdListItemMainView extends LinearLayout implements BindableView<AdModel>,
        OnCheckedChangeListener, OnClickListener, ListItemExpandListener,
        ListItemCollapseListener {
    private EventHandler mEventHandler;
    private AdModel mModel;

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mSummaryView;
    private CheckBox mCheckView;
    private ImageView mIndicatorCloseView;
    private ImageView mIndicatorOpenView;

    public AdListItemMainView(Context context) {
        this(context, null);
    }

    public AdListItemMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mNameView = (TextView) findViewById(R.id.name);
        mSummaryView = (TextView) findViewById(R.id.summary);
        mCheckView = (CheckBox) findViewById(R.id.check);
        mIndicatorCloseView = (ImageView) findViewById(R.id.indicator_close);
        mIndicatorOpenView = (ImageView) findViewById(R.id.indicator_open);

        setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(AdModel model) {
        mCheckView.setOnCheckedChangeListener(null);
        mModel = model;
        ApkIconHelper.getInstance(getContext()).loadFolderIcon(mIconView);
        mNameView.setText(model.getName());

        mSummaryView.setText(ExtraTextUtils.formatFileSize(getContext(), model.getFileSize()));

        mCheckView.setChecked(model.adviseDelete());
        mCheckView.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        mModel.setAdviseDelete(checked);
    }

    @Override
    public void onClick(View v) {
        final Integer position = (Integer) getTag(R.id.position);
        mEventHandler.sendEventMessage(EventType.EVENT_PERFORM_ITEM_CLICK,
                PerformItemClickEvent.create(this, position));
    }

    @Override
    public void willExpand(View v) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didExpand(View v) {
        mIndicatorCloseView.setVisibility(View.GONE);
        mIndicatorOpenView.setVisibility(View.VISIBLE);
    }

    @Override
    public void willCollapse(View v) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didCollapse(View v) {
        mIndicatorOpenView.setVisibility(View.GONE);
        mIndicatorCloseView.setVisibility(View.VISIBLE);
    }
}
